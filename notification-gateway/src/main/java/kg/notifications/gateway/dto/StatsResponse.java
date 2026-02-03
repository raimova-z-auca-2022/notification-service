package kg.notifications.gateway.dto;

import java.util.Map;

public record StatsResponse(
        long totalSent,
        long pending,
        long scheduled,
        double avgDeliverySeconds,
        Map<String, Integer> channelDistribution
) {}
