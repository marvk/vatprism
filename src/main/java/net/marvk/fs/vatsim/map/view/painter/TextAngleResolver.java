package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.VPos;
import javafx.scene.text.TextAlignment;

public class TextAngleResolver {
    public TextAngleResolver() {
    }

    public Octant octant(final double angle) {
        return Octant.of(angle);
    }

    public VPos vPos(final Octant octant) {
        return switch (octant.opposingQuadrant) {
            case TOP -> VPos.BOTTOM;
            case RIGHT, LEFT -> VPos.CENTER;
            case BOTTOM -> VPos.TOP;
        };
    }

    public TextAlignment align(final Octant octant) {
        return switch (octant) {
            case TOP_RIGHT, BOTTOM_RIGHT, TOP_LEFT, BOTTOM_LEFT -> switch (octant.opposingQuadrant) {
                case RIGHT -> TextAlignment.LEFT;
                case LEFT -> TextAlignment.RIGHT;
                case TOP, BOTTOM -> throw new AssertionError();
            };
            case RIGHT_TOP, RIGHT_BOTTOM -> TextAlignment.LEFT;
            case LEFT_BOTTOM, LEFT_TOP -> TextAlignment.RIGHT;
        };
    }

    public double xOffset(final Octant octant, final double offset) {
        return switch (octant.opposingQuadrant) {
            case TOP, BOTTOM -> 0;
            case RIGHT -> offset;
            case LEFT -> -offset;
        };
    }

    public double yOffset(final Octant octant, final double offset) {
        return switch (octant.opposingQuadrant) {
            case TOP -> -offset;
            case RIGHT, LEFT -> 0;
            case BOTTOM -> offset;
        };
    }

    public enum Octant {
        TOP_RIGHT(Quadrant.TOP, Quadrant.LEFT),
        RIGHT_TOP(Quadrant.RIGHT, Quadrant.BOTTOM),
        RIGHT_BOTTOM(Quadrant.RIGHT, Quadrant.TOP),
        BOTTOM_RIGHT(Quadrant.BOTTOM, Quadrant.LEFT),
        BOTTOM_LEFT(Quadrant.BOTTOM, Quadrant.RIGHT),
        LEFT_BOTTOM(Quadrant.LEFT, Quadrant.TOP),
        LEFT_TOP(Quadrant.LEFT, Quadrant.BOTTOM),
        TOP_LEFT(Quadrant.TOP, Quadrant.RIGHT);

        private final Quadrant insideQuadrant;
        private final Quadrant opposingQuadrant;

        Octant(final Quadrant insideQuadrant, final Quadrant opposingQuadrant) {
            this.insideQuadrant = insideQuadrant;
            this.opposingQuadrant = opposingQuadrant;
        }

        public static Octant of(final double angle) {
            final double a = limit(angle);
            if (a < 45) {
                return Octant.TOP_RIGHT;
            } else if (a < 90) {
                return Octant.RIGHT_TOP;
            } else if (a < 135) {
                return Octant.RIGHT_BOTTOM;
            } else if (a < 180) {
                return Octant.BOTTOM_RIGHT;
            } else if (a < 225) {
                return Octant.BOTTOM_LEFT;
            } else if (a < 270) {
                return Octant.LEFT_BOTTOM;
            } else if (a < 315) {
                return Octant.LEFT_TOP;
            } else {
                return Octant.TOP_LEFT;
            }
        }

        private static double limit(final double angle) {
            return ((angle % 360) + 360) % 360;
        }
    }

    public enum Quadrant {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT
    }
}
