package net.marvk.fs.vatsim.map.view.map;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FrameMetrics {
    private final List<Metric> metrics;
    private final Map<String, Metric> metricsMap;
    private final int maxFrames;

    public FrameMetrics(final Collection<String> names, final int maxFrames) {
        this.maxFrames = maxFrames;
        this.metrics = names.stream().map(name -> new Metric(name, maxFrames)).collect(Collectors.toList());
        this.metricsMap = metrics.stream().collect(Collectors.toMap(Metric::getName, Function.identity()));
    }

    public Metric getMetric(final String name) {
        return metricsMap.get(name);
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public int getMaxFrames() {
        return maxFrames;
    }

    public static class Metric {
        private final String name;

        private final List<Long> frameTimes;
        private final List<Long> unmodifiableFrameTimes;
        private final int maxFrames;

        public Metric(final String name, final int maxFrames) {
            this.name = name;
            this.maxFrames = maxFrames;
            this.frameTimes = new LinkedList<>();
            this.unmodifiableFrameTimes = Collections.unmodifiableList(frameTimes);
        }

        public String getName() {
            return name;
        }

        public double getLast() {
            return frameTimes.isEmpty() ? Double.NaN : frameTimes.get(frameTimes.size() - 1);
        }

        public void append(final long frameTime) {
            while (frameTimes.size() >= maxFrames) {
                frameTimes.remove(0);
            }

            frameTimes.add(frameTime);
        }

        public long max() {
            return frameTimes.stream().mapToLong(e -> e).max().orElse(0);
        }

        public double average() {
            return frameTimes.stream().mapToLong(e -> e).average().orElse(0);
        }

        public List<Long> getFrameTimes() {
            return unmodifiableFrameTimes;
        }

        public int getMaxFrames() {
            return maxFrames;
        }

        public int getCurrentNumberOfFrameTimes() {
            return frameTimes.size();
        }
    }
}
