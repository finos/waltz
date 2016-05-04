package com.khartec.waltz.common;

/**
 * Created by dwatkins on 29/04/2016.
 */
public class RangeBandUtilities {

    public static String toPrettyString(RangeBand<?> band) {
        return new StringBuilder()
                .append(band.getLow() == null ? "*" : band.getLow())
                .append(" - ")
                .append(band.getHigh() == null ? "*" : band.getHigh())
                .toString();
    }

}
