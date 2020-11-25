package net.marvk.fs.vatsim.map.view.map;

import javafx.geometry.Point2D;

public class MapVariables {
    public static final double WORLD_WIDTH = 360;
    public static final double WORLD_HALF_WIDTH = 180;
    public static final double WORLD_HEIGHT = 180;
    public static final double WORLD_HALF_HEIGHT = 90;

    public static final double WORLD_ASPECT_RATIO = WORLD_WIDTH / WORLD_HEIGHT;

    private double scale;

    private double viewWidth = 0;
    private double viewHeight = 0;

    private double viewHalfWidth = 0;
    private double viewHalfHeight = 0;

    private double worldCenterX = 0;
    private double worldCenterY = 0;

    private final double[] xBuf;
    private final double[] yBuf;

    private double aspectScaleY;

    public MapVariables() {
        this(32000);
    }

    public MapVariables(final int bufferSize) {
        this.xBuf = new double[bufferSize];
        this.yBuf = new double[bufferSize];
    }

    public double toCanvasX(final double modelX) {
        final double zeroStart = (modelX + worldCenterX) * scale + WORLD_HALF_WIDTH;
        final double normalized = zeroStart / WORLD_WIDTH;

        return normalized * viewWidth + 0.5;
    }

    public Point2D toWorld(final Point2D canvas) {
        return new Point2D(
                toWorldX(canvas.getX()),
                toWorldY(canvas.getY())
        );
    }

    public Point2D toCanvas(final Point2D world) {
        return new Point2D(
                toCanvasX(world.getX()),
                toCanvasY(world.getY())
        );
    }

    public double toWorldX(final double canvasX) {
        return ((((canvasX - 0.5) / viewWidth) * WORLD_WIDTH - WORLD_HALF_WIDTH) / scale) - worldCenterX;
    }

    public double toCanvasY(final double modelY) {
        final double zeroStart = ((modelY + worldCenterY) * scale * aspectScaleY() + WORLD_HALF_HEIGHT);
        final double normalized = zeroStart / WORLD_HEIGHT;

        return viewHeight - normalized * viewHeight + 0.5;
    }

    public double toWorldY(final double canvasY) {
        return ((((1 - (canvasY - 0.5) / viewHeight) * WORLD_HEIGHT - WORLD_HALF_HEIGHT) / scale) / aspectScaleY()) - worldCenterY;
    }

    public double aspectScaleY() {
        return aspectScaleY;
    }

    void setScale(final double scale) {
        this.scale = scale;
    }

    void setViewSize(final double width, final double height) {
        this.viewWidth = width;
        this.viewHeight = height;

        this.viewHalfHeight = width / 2.0;
        this.viewHalfWidth = height / 2.0;

        this.aspectScaleY = viewWidth / viewHeight / WORLD_ASPECT_RATIO;
    }

    void setViewWidth(final double width) {
        setViewSize(width, viewHeight);
    }

    void setViewHeight(final double height) {
        setViewSize(viewWidth, height);
    }

    void setWorldCenter(final double x, final double y) {
        this.worldCenterX = x;
        this.worldCenterY = y;
    }

    void setWorldCenter(final Point2D worldCenter) {
        setWorldCenter(worldCenter.getX(), worldCenter.getY());
    }

    public double getScale() {
        return scale;
    }

    public double getViewWidth() {
        return viewWidth;
    }

    public double getViewHeight() {
        return viewHeight;
    }

    public double getViewHalfWidth() {
        return viewHalfWidth;
    }

    public double getViewHalfHeight() {
        return viewHalfHeight;
    }

    public double getWorldCenterX() {
        return worldCenterX;
    }

    public double getWorldCenterY() {
        return worldCenterY;
    }

    public double[] getXBuf() {
        return xBuf;
    }

    public double[] getYBuf() {
        return yBuf;
    }
}
