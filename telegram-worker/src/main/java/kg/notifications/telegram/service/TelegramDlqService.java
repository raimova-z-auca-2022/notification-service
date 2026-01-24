package kg.notifications.telegram.service;

public interface TelegramDlqService {
    void sendToDlq(Object payload, String dlqQueueName, String originalQueue, String exceptionMsg);
}