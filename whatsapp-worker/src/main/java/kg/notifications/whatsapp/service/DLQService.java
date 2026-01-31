package kg.notifications.whatsapp.service;


public interface DLQService {
    void sendToDlq(Object payload, String dlqQueueName, String originalQueue, String exceptionMsg);
}