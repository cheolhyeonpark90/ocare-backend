package com.ocare.ocarebackend.domain.health.service;

import com.ocare.ocarebackend.domain.health.HealthLogRepository;
import com.ocare.ocarebackend.domain.health.dto.HealthStatsSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthStatsService {

    private final HealthLogRepository healthLogRepository;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    public List<HealthStatsSummary> getDailyStats(String recordKey) {
        return getStats(recordKey, "daily", (key) -> healthLogRepository.findDailyStatsByRecordKey(key));
    }

    public List<HealthStatsSummary> getMonthlyStats(String recordKey) {
        return getStats(recordKey, "monthly", (key) -> healthLogRepository.findMonthlyStatsByRecordKey(key));
    }

    private List<HealthStatsSummary> getStats(String recordKey, String type,
            java.util.function.Function<String, List<HealthStatsSummary>> dbFetcher) {
        String stepsKey = "stats:" + type + ":" + recordKey + ":steps";
        String distanceKey = "stats:" + type + ":" + recordKey + ":distance";
        String caloriesKey = "stats:" + type + ":" + recordKey + ":calories";

        Map<Object, Object> stepsMap = redisTemplate.opsForHash().entries(stepsKey);
        Map<Object, Object> distanceMap = redisTemplate.opsForHash().entries(distanceKey);
        Map<Object, Object> caloriesMap = redisTemplate.opsForHash().entries(caloriesKey);

        if (!stepsMap.isEmpty()) {
            return mergeStats(stepsMap, distanceMap, caloriesMap);
        }

        List<HealthStatsSummary> stats = dbFetcher.apply(recordKey);

        if (!stats.isEmpty()) {
            Map<String, String> sMap = new HashMap<>();
            Map<String, String> dMap = new HashMap<>();
            Map<String, String> cMap = new HashMap<>();

            for (HealthStatsSummary s : stats) {
                String period = s.getPeriod();
                sMap.put(period, String.valueOf(s.getSteps() != null ? s.getSteps() : 0));
                dMap.put(period, String.valueOf(s.getDistance() != null ? s.getDistance() : 0.0));
                cMap.put(period, String.valueOf(s.getCalories() != null ? s.getCalories() : 0.0));
            }

            redisTemplate.opsForHash().putAll(stepsKey, sMap);
            redisTemplate.opsForHash().putAll(distanceKey, dMap);
            redisTemplate.opsForHash().putAll(caloriesKey, cMap);

            redisTemplate.expire(stepsKey, java.time.Duration.ofHours(1));
            redisTemplate.expire(distanceKey, java.time.Duration.ofHours(1));
            redisTemplate.expire(caloriesKey, java.time.Duration.ofHours(1));
        }

        return stats;
    }

    private List<HealthStatsSummary> mergeStats(Map<Object, Object> sMap, Map<Object, Object> dMap,
            Map<Object, Object> cMap) {
        Set<String> periods = new HashSet<>();
        sMap.keySet().forEach(k -> periods.add((String) k));
        dMap.keySet().forEach(k -> periods.add((String) k));
        cMap.keySet().forEach(k -> periods.add((String) k));

        List<HealthStatsSummary> result = new ArrayList<>();
        for (String p : periods) {
            long steps = sMap.containsKey(p) ? Long.parseLong((String) sMap.get(p)) : 0L;
            double dist = dMap.containsKey(p) ? Double.parseDouble((String) dMap.get(p)) : 0.0;
            double cal = cMap.containsKey(p) ? Double.parseDouble((String) cMap.get(p)) : 0.0;

            result.add(com.ocare.ocarebackend.domain.health.dto.HealthStatsDto.builder()
                    .period(p)
                    .steps(steps)
                    .distance(dist)
                    .calories(cal)
                    .build());
        }
        // Sort by period
        result.sort(Comparator.comparing(HealthStatsSummary::getPeriod));
        return result;
    }
}
