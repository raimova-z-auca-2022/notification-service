package kg.notifications.gateway.dto;

import java.util.Map;

public record ChannelStatsResponse(
        Map<String, Integer> distribution
) {}
