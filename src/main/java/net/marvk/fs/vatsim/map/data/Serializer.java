package net.marvk.fs.vatsim.map.data;

public interface Serializer<T> {
    String serialize(final T t);
}
