package net.marvk.fs.vatsim.map.view.painter;

import net.marvk.fs.vatsim.map.view.map.MapVariables;

public abstract class MapPainter<T> implements Painter<T> {
    protected final MapVariables mapVariables;
    protected final PainterHelper painterHelper;

    public MapPainter(final MapVariables mapVariables) {
        this.mapVariables = mapVariables;
        this.painterHelper = new PainterHelper(mapVariables);
    }
}
