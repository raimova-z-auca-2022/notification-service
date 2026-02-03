package kg.notifications.gateway.service.impl;

import kg.notifications.gateway.dto.ChannelStatsResponse;
import kg.notifications.gateway.dto.StatsResponse;
import kg.notifications.gateway.repository.StatsRepository;
import kg.notifications.gateway.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public ChannelStatsResponse getChannelStats() {

        List<Map<String, Object>> rows =
                statsRepository.channelDistributionLast7Days();

        long totalInPeriod = rows.stream()
                .mapToLong(r -> ((Number) r.get("cnt")).longValue())
                .sum();

        Map<String, Integer> distribution = new HashMap<>();

        if (totalInPeriod == 0) {
            fillDistribution(
                    statsRepository.channelDistributionOverall(),
                    distribution
            );
        } else {
            fillDistribution(rows, distribution);
        }

        return new ChannelStatsResponse(distribution);
    }


    @Override
    public StatsResponse getStats() {

        long totalSent = defaultLong(statsRepository.countSent());
        long pending = defaultLong(statsRepository.countPending());
        long scheduled = defaultLong(statsRepository.countScheduled());
        double avgDelivery = defaultDouble(statsRepository.avgDeliveryTime());

        List<Map<String, Object>> rows =
                statsRepository.channelDistributionLast7Days();

        long totalInPeriod = rows.stream()
                .mapToLong(r -> ((Number) r.get("cnt")).longValue())
                .sum();

        Map<String, Integer> distribution = new HashMap<>();

        if (totalInPeriod == 0) {
            fillDistribution(
                    statsRepository.channelDistributionOverall(),
                    distribution
            );
        } else {
            fillDistribution(rows, distribution);
        }

        return new StatsResponse(
                totalSent,
                pending,
                scheduled,
                avgDelivery,
                distribution
        );
    }

    private void fillDistribution(List<Map<String, Object>> rows,
                                  Map<String, Integer> distribution) {

        long total = rows.stream()
                .mapToLong(r -> ((Number) r.get("cnt")).longValue())
                .sum();

        for (Map<String, Object> r : rows) {
            String type = (String) r.get("type");
            long cnt = ((Number) r.get("cnt")).longValue();
            int pct = total == 0 ? 0 : (int) Math.round(100.0 * cnt / total);
            distribution.put(type, pct);
        }
    }

    private long defaultLong(Long v) {
        return v == null ? 0L : v;
    }

    private double defaultDouble(Double v) {
        return v == null ? 0.0 : v;
    }
}
