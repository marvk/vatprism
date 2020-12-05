package net.marvk.fs.vatsim.map.view;

import java.util.ArrayList;
import java.util.List;

public class History<E> {
    private final List<E> items = new ArrayList<>();
    private int index = -1;

    public History() {
    }

    public void clear() {
        index = -1;
        items.clear();
    }

    public void add(final E e) {
        index += 1;
        items.subList(index, items.size()).clear();
        items.add(e);
    }

    public E previous() {
        if (items.isEmpty()) {
            return null;
        }

        shift(-1);

        return items.get(index);
    }

    public E current() {
        if (items.isEmpty()) {
            return null;
        }

        return items.get(index);
    }

    public E next() {
        if (items.isEmpty()) {
            return null;
        }

        shift(1);

        return items.get(index);
    }

    public E peekNext() {
        return get(index + 1);
    }

    public E peekPrevious() {
        return get(index - 1);
    }

    public boolean nextAvailable() {
        return index < items.size() - 1;
    }

    public boolean previousAvailable() {
        return index > 0;
    }

    private void shift(final int d) {
        index = Math.max(0, Math.min(index + d, items.size() - 1));
    }

    private E get(final int index) {
        if (items.isEmpty()) {
            return null;
        }
        final int i = Math.max(0, Math.min(index, items.size() - 1));
        return items.get(i);
    }
}
