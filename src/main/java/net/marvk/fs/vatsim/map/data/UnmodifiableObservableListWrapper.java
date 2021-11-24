package net.marvk.fs.vatsim.map.data;

import com.sun.javafx.collections.SourceAdapterChange;
import javafx.collections.*;

import java.util.Collection;

public class UnmodifiableObservableListWrapper<E> extends ObservableListBase<E> implements ObservableList<E> {
    private final ObservableList<E> backingList;
    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<E> listener;
    private final ObservableList<E> backingListReadOnlyView;

    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    public UnmodifiableObservableListWrapper(final ObservableList<E> backingList) {
        this.backingList = backingList;
        this.listener = change -> fireChange(new SourceAdapterChange<E>(UnmodifiableObservableListWrapper.this, change));
        this.backingList.addListener(new WeakListChangeListener<E>(listener));
        this.backingListReadOnlyView = FXCollections.unmodifiableObservableList(backingList);
    }

    @SuppressWarnings({"SuspiciousGetterSetter", "AssignmentOrReturnOfFieldWithMutableType"})
    public ObservableList<E> getReadOnlyList() {
        return backingListReadOnlyView;
    }

    @Override
    public E get(final int index) {
        return backingList.get(index);
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @SafeVarargs
    @Override
    public final boolean addAll(final E... elements) {
        return backingList.addAll(elements);
    }

    @SafeVarargs
    @Override
    public final boolean setAll(final E... elements) {
        return backingList.setAll(elements);
    }

    @Override
    public boolean setAll(final Collection<? extends E> collection) {
        return backingList.setAll(collection);
    }

    @SafeVarargs
    @Override
    public final boolean removeAll(final E... elements) {
        return backingList.removeAll(elements);
    }

    @SafeVarargs
    @Override
    public final boolean retainAll(final E... elements) {
        return backingList.retainAll(elements);
    }

    @Override
    public void remove(final int from, final int to) {
        backingList.remove(from, to);
    }
}
