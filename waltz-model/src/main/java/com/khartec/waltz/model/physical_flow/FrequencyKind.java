package com.khartec.waltz.model.physical_flow;

/**
 * Represents the frequency a data flow is 'activated'
 * <p/>
 * The UNKNOWN option is not intended to be exposed as
 * as selectable choice for users.  It is intended to be
 * used when bulk importing from systems which do not have
 * any equivalent frequency representation
 */
public enum FrequencyKind {
    ON_DEMAND, // pull
    REAL_TIME, // push
    INTRA_DAY,
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    BIANNUALLY,
    YEARLY,
    UNKNOWN
}
