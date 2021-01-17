package net.marvk.fs.vatsim.map.data;

public interface Deserializer<T> {
    T deserialize(final String s);
}
