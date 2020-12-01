package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class RelationshipReadOnlyObjectWrapper<E> extends ReadOnlyObjectWrapper<E> {
    private final Consumer<E> addTo;
    private final Consumer<E> removeFrom;

    private RelationshipReadOnlyObjectWrapper(final Consumer<E> addTo, final Consumer<E> removeFrom) {
        this.addTo = addTo;
        this.removeFrom = removeFrom;
    }

    @Override
    public void set(final E newValue) {
        final E oldValue = getValue();
        if (Objects.equals(newValue, oldValue)) {
            return;
        }
        super.set(newValue);

        if (oldValue != null) {
            removeFrom.accept(oldValue);
        }

        if (newValue != null) {
            addTo.accept(newValue);
        }
    }

    public static <E, Other> RelationshipReadOnlyObjectWrapper<E> withOtherList(final Other parent, final Function<E, List<Other>> otherMapper) {
        return new RelationshipReadOnlyObjectWrapper<>(
                e -> otherMapper.apply(e).add(parent),
                e -> otherMapper.apply(e).remove(parent)
        );
    }

    public static <E, Other> RelationshipReadOnlyObjectWrapper<E> withOtherProperty(final Other parent, final Function<E, ObjectProperty<Other>> otherMapper) {
        return new RelationshipReadOnlyObjectWrapper<>(
                e -> otherMapper.apply(e).set(parent),
                e -> otherMapper.apply(e).set(null)
        );
    }
}
