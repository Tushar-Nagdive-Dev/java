package org.banking.app.domain.port;

/** Port for sending notifications (email/SMS/push). */
public interface NotificationPort {
    void send(String to, String message);
}
