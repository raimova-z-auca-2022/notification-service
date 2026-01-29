package kg.notifications.email.service;

public interface DlqService {
    void sendToDlq(Object payload, String dlqQueueName, String originalQueue, String exceptionMsg);
}
