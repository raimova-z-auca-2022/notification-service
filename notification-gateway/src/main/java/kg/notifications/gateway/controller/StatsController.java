package kg.notifications.gateway.controller;

import kg.notifications.gateway.dto.ChannelStatsResponse;
import kg.notifications.gateway.dto.StatsResponse;
import kg.notifications.gateway.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/stats/channels")
    public ChannelStatsResponse channelStats() {
        return statsService.getChannelStats();
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return statsService.getStats();
    }

}
