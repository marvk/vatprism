package net.marvk.fs.vatsim.map.view.map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Value;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Value
public class PainterMetric implements Iterable<PainterMetric.Counter> {
    Counter strokePolyline = new Counter("strokePolyline");
    Counter strokePolygon = new Counter("strokePolygon");
    Counter fillPolygon = new Counter("fillPolygon");
    Counter fillText = new Counter("fillText");
    Counter fillOval = new Counter("fillOval");
    Counter strokeOval = new Counter("strokeOval");
    Counter strokeLine = new Counter("strokeLine");
    Counter strokeRect = new Counter("strokeRect");
    Counter fillRect = new Counter("fillRect");

    @Getter(AccessLevel.PRIVATE)
    List<Counter> counters = List.of(
            strokePolyline,
            strokePolygon,
            fillPolygon,
            fillText,
            fillOval,
            strokeOval,
            strokeLine,
            strokeRect,
            fillRect
    );

    @Override
    public Iterator<Counter> iterator() {
        return counters.iterator();
    }

    @Data
    public static class Counter {
        private final String name;
        private int count = 0;

        public int getCount() {
            return count;
        }

        public void increment() {
            count += 1;
        }

        private void increment(final Counter other) {
            count += other.count;
        }
    }

    public static PainterMetric ofMetrics(final Collection<PainterMetric> metrics) {
        final PainterMetric result = new PainterMetric();

        for (final PainterMetric metric : metrics) {
            result.strokePolyline.increment(metric.strokePolyline);
            result.strokePolygon.increment(metric.strokePolygon);
            result.fillPolygon.increment(metric.fillPolygon);
            result.fillText.increment(metric.fillText);
            result.fillOval.increment(metric.fillOval);
            result.strokeOval.increment(metric.strokeOval);
            result.strokeLine.increment(metric.strokeLine);
            result.strokeRect.increment(metric.strokeRect);
            result.fillRect.increment(metric.fillRect);
        }

        return result;
    }
}
