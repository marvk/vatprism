package net.marvk.fs.vatsim.map.view;

import de.saxsys.mvvmfx.MvvmFX;
import net.marvk.fs.vatsim.map.data.Data;

import java.util.function.Consumer;

public final class Notifications {
    public static final DataNotification<Data> PAN_TO_DATA = new DataNotification<>("PAN_TO_DATA");
    public static final DataNotification<Data> SET_DATA_DETAIL = new DataNotification<>("SET_DATA_DETAIL");
    public static final DataNotification<Integer> SWITCH_TO_TAB = new DataNotification<>("SWITCH_TO_TAB");
    public static final VoidNotification REPAINT = new VoidNotification("REPAINT");
    public static final VoidNotification RELOAD_CLIENTS = new VoidNotification("RELOAD_CLIENTS");
    public static final VoidNotification CLIENTS_RELOADED = new VoidNotification("CLIENTS_RELOADED");
    public static final VoidNotification SEARCH = new VoidNotification("SEARCH");

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

    public static class DataNotification<E> {
        private final String message;

        public DataNotification(final String message) {
            this.message = message;
        }

        @SuppressWarnings("unchecked")
        public void subscribe(final Consumer<E> onNotification) {
            MvvmFX.getNotificationCenter().subscribe(message, (key, payload) -> onNotification.accept((E) payload[0]));
        }

        public void publish(final E e) {
            MvvmFX.getNotificationCenter().publish(message, e);
        }
    }
}
