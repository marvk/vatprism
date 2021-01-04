package net.marvk.fs.vatsim.map.view.map;

import lombok.Value;

@Value
public class PainterMetric {
    Counter strokePolyline = new Counter();
    Counter strokePolygon = new Counter();
    Counter fillPolygon = new Counter();
    Counter fillText = new Counter();
    Counter fillOval = new Counter();
    Counter strokeOval = new Counter();
    Counter strokeLine = new Counter();
    Counter strokeRect = new Counter();
    Counter fillRect = new Counter();

    public static class Counter {
        private int count = 0;

        public int getCount() {
            return count;
        }

        public void increment() {
            count++;
        }
    }
}
