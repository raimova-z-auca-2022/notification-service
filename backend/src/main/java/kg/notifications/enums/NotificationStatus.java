package kg.notifications.enums;


public enum NotificationStatus {
    PENDING(1),
    SENDING(2),
    SENT(3),
    FAILED(4);

    private final int id;

    NotificationStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static NotificationStatus fromId(int id) {
        for (NotificationStatus s : values()) {
            if (s.id == id) {
                return s;
            }
        }
        return PENDING;
    }
}
