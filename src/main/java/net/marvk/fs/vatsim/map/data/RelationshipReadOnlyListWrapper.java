package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class RelationshipReadOnlyListWrapper<E> extends ReadOnlyListWrapper<E> {
    private final Consumer<E> addTo;
    private final Consumer<E> removeFrom;

    public RelationshipReadOnlyListWrapper(final Consumer<E> addTo, final Consumer<E> removeFrom) {
        super(FXCollections.observableArrayList());
        this.addTo = addTo;
        this.removeFrom = removeFrom;
    }

    @Override
    public void clear() {
        final List<E> items = new ArrayList<>(this);

        super.clear();

        items.forEach(removeFrom);
    }

    @Override
    public boolean add(final E element) {
        if (contains(element)) {
            return false;
        }

        super.add(element);

        addTo.accept(element);

        return true;
    }

    @Override
    public boolean remove(final Object obj) {
        if (!contains(obj)) {
            return false;
        }

        final boolean remove = super.remove(obj);

        removeFrom.accept((E) obj);

        return remove;
    }

    @Override
    public boolean setAll(final Collection<? extends E> elements) {
        clear();
        elements.forEach(this::add);
        return true;
    }

    boolean regularAdd(final E e) {
        return super.add(e);
    }

    boolean regularRemove(final Object obj) {
        return super.remove(obj);
    }

    public static <E, Other> RelationshipReadOnlyListWrapper<E> withOtherList(final Other parent, final Function<E, List<Other>> otherMapper) {
        return new RelationshipReadOnlyListWrapper<>(
                e -> otherMapper.apply(e).add(parent),
                e -> otherMapper.apply(e).remove(parent)
        );
    }

    public static <E, Other> RelationshipReadOnlyListWrapper<E> withOtherProperty(final Other parent, final Function<E, ObjectProperty<Other>> otherMapper) {
        return new RelationshipReadOnlyListWrapper<>(
                e -> otherMapper.apply(e).set(parent),
                e -> otherMapper.apply(e).set(null)
        );
    }
}
