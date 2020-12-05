package net.marvk.fs.vatsim.map.view.datadetail;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.data.PositionDataVisitor;

import java.util.Optional;

public class DataDetailSubViewModel<D extends Data> implements ViewModel {
    protected final ObjectProperty<D> client = new SimpleObjectProperty<>();
    protected final NotificationCenter notificationCenter;
    protected final PositionDataVisitor positionDataVisitor;

    @Inject
    public DataDetailSubViewModel(final NotificationCenter notificationCenter, final PositionDataVisitor positionDataVisitor) {
        this.notificationCenter = notificationCenter;
        this.positionDataVisitor = positionDataVisitor;
    }

    public D getData() {
        return client.get();
    }

    public ObjectProperty<D> dataProperty() {
        return client;
    }

    public void setData(final D data) {
        this.client.set(data);
    }

    public Optional<Point2D> getPosition() {
        return positionDataVisitor.visit(getData());
    }

    public void goTo() {
        final Optional<Point2D> maybePosition = getPosition();
        maybePosition.ifPresent(e -> notificationCenter.publish("PAN_TO_POSITION", e));
    }
}
