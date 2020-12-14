package net.marvk.fs.vatsim.map.view.painter;

public abstract class DisableablePainter<T> implements Painter<T> {
    @Parameter("Enabled")
    protected boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
