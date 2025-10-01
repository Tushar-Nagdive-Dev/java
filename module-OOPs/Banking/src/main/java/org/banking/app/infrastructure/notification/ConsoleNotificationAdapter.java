package org.banking.app.infrastructure.notification;

import org.banking.app.domain.port.NotificationPort;

/** Console-based notification adapter for the demo. */
public final class ConsoleNotificationAdapter implements NotificationPort {
    @Override public void send(String to, String message) {
        System.out.printf("Notify -> %s: %s%n", to, message);
    }
}
