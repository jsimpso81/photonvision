/*
 * Copyright (C) Photon Vision.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.photonvision.common.util.math;

import edu.wpi.first.util.WPIUtilJNI;

public class MathUtils {
    MathUtils() {}

    public static double toSlope(Number angle) {
        return Math.atan(Math.toRadians(angle.doubleValue() - 90));
    }

    public static int safeDivide(int quotient, int divisor) {
        if (divisor == 0) {
            return 0;
        } else {
            return quotient / divisor;
        }
    }

    public static double roundTo(double value, int to) {
        double toMult = Math.pow(10, to);
        return (double) Math.round(value * toMult) / toMult;
    }

    public static double nanosToMillis(long nanos) {
        return nanos / 1000000.0;
    }

    public static double nanosToMillis(double nanos) {
        return nanos / 1000000.0;
    }

    public static long millisToNanos(long millis) {
        return millis * 1000000;
    }

    public static long microsToNanos(long micros) {
        return micros * 1000;
    }

    public static double map(
            double value, double in_min, double in_max, double out_min, double out_max) {
        return (value - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public static int map(int value, int inMin, int inMax, int outMin, int outMax) {
        return (int) Math.floor(map((double) value, inMin, inMax, outMin, outMax) + 0.5);
    }

    /**
     * Linearly interpolates between two values.
     *
     * @param startValue The start value.
     * @param endValue The end value.
     * @param t The fraction for interpolation.
     * @return The interpolated value.
     */
    @SuppressWarnings("ParameterName")
    public static double lerp(double startValue, double endValue, double t) {
        return startValue + (endValue - startValue) * t;
    }

    public static long wpiNanoTime() {
        return microsToNanos(WPIUtilJNI.now());
    }
}
