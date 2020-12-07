package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import net.marvk.fs.vatsim.map.view.map.FrameMetrics;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.List;
import java.util.stream.DoubleStream;

public class FrameMetricsPainter extends MapPainter<FrameMetrics> {
    private static final double NANOS_IN_MILLI = 1000000.;
    @Parameter("Show")
    private boolean show = true;

    @Parameter("Show Averages")
    private boolean showAverages = true;

    private static Color[] COLORS = new Color[]{
            Color.web("#C6C6C6"),
            Color.web("#FDB2C1"),
            Color.web("#F4B9A1"),
            Color.web("#DEC38B"),
            Color.web("#C0CD89"),
            Color.web("#9AD59B"),
            Color.web("#77D8B9"),
            Color.web("#6CD7D7"),
            Color.web("#88D0EF"),
            Color.web("#B4C5FA"),
            Color.web("#DBB9F7"),
            Color.web("#F5B2E5")
    };

    @Parameter(value = "Height", min = 0)
    private double chartHeight = 300;

    private int x = 10;
    private int y = 10;
    private int borderWidth = 20;

    public FrameMetricsPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext c, final FrameMetrics frameMetrics) {
        if (!show) {
            return;
        }

        paintChart(c, frameMetrics, x, y);
    }

    private void paintChart(final GraphicsContext c, final FrameMetrics frameMetrics, final int x, final int y) {
        final FrameMetrics.Metric total = frameMetrics.getMetric("Total");
        final List<FrameMetrics.Metric> metrics = frameMetrics.getMetrics();

        final long maxNanos = total.max();

        final int offset = frameMetrics.getMaxFrames() - total.getCurrentNumberOfFrameTimes();

        final double xOffset = x + borderWidth;
        final double yOffset = y + borderWidth;

        final int chartWidth = frameMetrics.getMaxFrames();

        c.setFill(Color.GRAY.darker().darker());
        c.fillRect(x, y, frameMetrics.getMaxFrames() + borderWidth * (5 + (showAverages ? 10 : 0)), getChartHeight() + borderWidth * 2);
        c.setFill(Color.GRAY);
        c.fillRect(xOffset, yOffset, chartWidth, getChartHeight());

        c.setFill(Color.PINK);

        final List<Long> frameTimes = total.getFrameTimes();
        for (int i = 0; i < frameTimes.size(); i++) {
            final long totalNanos = frameTimes.get(i);
            final double totalHeight = height(maxNanos, totalNanos);
            c.setFill(COLORS[0]);
            c.fillRect(
                    xOffset + offset + i,
                    heightInCanvas(yOffset, totalHeight),
                    1,
                    totalHeight
            );

            double barOffset = 0;

            for (int j = 0; j < metrics.size(); j++) {
                final FrameMetrics.Metric metric = metrics.get(j);
                if ("Total".equals(metric.getName())) {
                    continue;
                }
                final long currentNanos = metric.getFrameTimes().get(i);
                final double currentHeight = height(maxNanos, currentNanos);

                c.setFill(COLORS[j]);
                c.fillRect(
                        xOffset + offset + i,
                        heightInCanvas(yOffset, currentHeight) - barOffset,
                        1,
                        currentHeight
                );

                barOffset += currentHeight;
            }
        }

        final double averageDrawNanos = averages(metrics).sum();

        if (showAverages) {
            final double max = averages(metrics).max().orElse(0);

            for (int i = 0; i < metrics.size(); i++) {
                final FrameMetrics.Metric metric = metrics.get(i);
                final double value;
                final Color color;
                final String name;
                if ("Total".equals(metric.getName())) {
                    name = "Residual";
                    value = metric.average() - averageDrawNanos;
                    color = COLORS[i];
                } else {
                    name = metric.getName();
                    value = metric.average();
                    color = new Color(0.5 + 0.5 * (value / max), 0.5, 0.5, 1);
                }

                c.setTextAlign(TextAlignment.RIGHT);
                c.setFill(COLORS[i]);
                final double yCur = yOffset + metrics.size() * 20 - i * 20;
                final double xCur = borderWidth * 10;
                c.fillText(name, xOffset + chartWidth + xCur, yCur);

                c.setTextAlign(TextAlignment.LEFT);
                c.setFill(color);
                c.fillText(nanoString(value), xOffset + chartWidth + xCur + 2, yCur);
            }
        }

        c.setFill(Color.GRAY);
        c.setTextAlign(TextAlignment.LEFT);
        c.setTextBaseline(VPos.CENTER);

        final boolean isHuge = maxNanos > 100 * NANOS_IN_MILLI;
        final double delta = (isHuge ? 100 : 10) * NANOS_IN_MILLI;
        final double padding = (isHuge ? 20 : 2) * NANOS_IN_MILLI;
        for (int labelNanos = 0; labelNanos < maxNanos - padding; labelNanos += delta) {
            final double currentHeight = height(maxNanos, labelNanos);
            drawLabel(c,
                    labelNanos,
                    xOffset + chartWidth,
                    heightInCanvas(yOffset, currentHeight)
            );
        }

        drawLabel(c,
                maxNanos,
                xOffset + chartWidth,
                yOffset
        );

        drawLine(c, total.average(), maxNanos, xOffset, yOffset, chartWidth, Color.BLACK);

        drawLine(c, averageDrawNanos, maxNanos, xOffset, yOffset, chartWidth, Color.BLACK);
    }

    private double getChartHeight() {
        return showAverages ? Math.max(250, chartHeight) : chartHeight;
    }

    private static DoubleStream averages(final List<FrameMetrics.Metric> metrics) {
        return metrics
                .stream()
                .filter(e -> !"Total".equals(e.getName()))
                .mapToDouble(FrameMetrics.Metric::average);
    }

    private void drawLine(final GraphicsContext c, final double value, final long max, final double xOffset, final double yOffset, final int width, final Color color) {
        c.setFill(color);
        final double averageY = heightInCanvas(yOffset, height(max, value));
        c.fillRect(
                xOffset,
                averageY,
                width,
                1
        );

        drawLabel(c,
                value,
                xOffset + width,
                averageY
        );
    }

    private double heightInCanvas(final double yOffset, final double currentHeight) {
        return yOffset + getChartHeight() - currentHeight;
    }

    private double height(final double maxNanos, final double nanos) {
        return (toMillis(nanos) / toMillis(maxNanos)) * getChartHeight();
    }

    private static void drawLabel(final GraphicsContext c, final double nanos, final double x, final double y) {
        c.fillText(nanoString(nanos), x, y);
    }

    private static String nanoString(final double nanos) {
        return String.format("%.2fms", toMillis(nanos));
    }

    private static double toMillis(final double nanos) {
        return nanos / NANOS_IN_MILLI;
    }
}
