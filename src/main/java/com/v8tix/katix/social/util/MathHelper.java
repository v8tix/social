package com.v8tix.katix.social.util;

import java.util.List;
import java.util.Random;

import static java.lang.Math.ceil;

public interface MathHelper {

    Random rand = new Random();

    static int getRandomInt(final int lo, final int hi) {
        return rand.nextInt(hi - lo + 1) + lo;
    }

    static double longToDouble(final long value) {
        return (double) value;
    }

    static long doubleToLong(final double value) {
        return (long) ceil(value);
    }

    static Object getRandomObject(List objects){
        final int objectsSize = objects.size();
        final int randomInt = getRandomInt(0, objectsSize - 1);
        return objects.get(randomInt);
    }

}
