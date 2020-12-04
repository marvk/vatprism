package net.marvk.fs.vatsim.map.data;

public interface Data {
    <R> R visit(final DataVisitor<R> visitor);
}
