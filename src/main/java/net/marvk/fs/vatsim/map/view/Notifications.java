package net.marvk.fs.vatsim.map.view;

import de.saxsys.mvvmfx.MvvmFX;
import net.marvk.fs.vatsim.map.data.Data;

import java.util.function.Consumer;

public final class Notifications {
    public static final Notification<Data> PAN_TO_DATA = new Notification<>("PAN_TO_DATA");
    public static final Notification<Data> SET_DATA_DETAIL = new Notification<>("SET_DATA_DETAIL");
    public static final VoidNotification REPAINT = new VoidNotification("REPAINT");
    public static final VoidNotification RELOAD_CLIENTS = new VoidNotification("RELOAD_CLIENTS");

    private Notifications() {
        throw new AssertionError("No instances of utility class " + Notifications.class);
    }

    public static class VoidNotification {
        private final String message;

        public VoidNotification(final String message) {
            this.message = message;
        }

        public void subscribe(final Runnable onNotification) {
            MvvmFX.getNotificationCenter().subscribe(message, (key, payload) -> onNotification.run());
        }

        public void publish() {
            MvvmFX.getNotificationCenter().publish(message);
        }
    }

    public static class Notification<E> {
        private final String message;

        public Notification(final String message) {
            this.message = message;
        }

        public void subscribe(final Consumer<E> onNotification) {
            MvvmFX.getNotificationCenter().subscribe(message, (key, payload) -> onNotification.accept((E) payload[0]));
        }

        public void publish(final E e) {
            MvvmFX.getNotificationCenter().publish(message, e);
        }
    }
}
