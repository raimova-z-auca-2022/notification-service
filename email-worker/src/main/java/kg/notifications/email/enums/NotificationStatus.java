package kg.notifications.email.enums;

import lombok.Getter;

@Getter
public enum NotificationStatus {
    PENDING(1),
    SENT(2),
    FAILED(3);

    private final int id;

    NotificationStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
