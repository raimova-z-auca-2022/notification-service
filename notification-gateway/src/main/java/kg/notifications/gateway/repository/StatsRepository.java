package kg.notifications.gateway.repository;

import java.util.List;
import java.util.Map;

public interface StatsRepository {

    Long countSent();

    Long countPending();

    Long countScheduled();

    Double avgDeliveryTime();

    List<Map<String, Object>> channelDistributionLast7Days();

    List<Map<String, Object>> channelDistributionOverall();
}
