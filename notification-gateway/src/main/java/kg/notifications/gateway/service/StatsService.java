package kg.notifications.gateway.service;

import kg.notifications.gateway.dto.ChannelStatsResponse;
import kg.notifications.gateway.dto.StatsResponse;

public interface StatsService {
    StatsResponse getStats();

    ChannelStatsResponse getChannelStats();
}
