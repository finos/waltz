package com.khartec.waltz.model.physical_flow;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.common.EnumParser;

public class FrequencyKindParser extends EnumParser<FrequencyKind> {

    private static final Aliases<FrequencyKind> defaultAliases = new Aliases<>()
            .register(FrequencyKind.YEARLY, "ANNUALLY")
            .register(FrequencyKind.BIANNUALLY, "BIANNUAL", "BIANUALY")
            .register(FrequencyKind.DAILY, "DAILY-WEEKDAYS")
            .register(FrequencyKind.INTRA_DAY, "INTRADAY")
            .register(FrequencyKind.ON_DEMAND, "PER REQUEST", "REQUEST BASIS", "AD HOC", "ON REQUEST")
            .register(FrequencyKind.MONTHLY, "MONTHLY")
            .register(FrequencyKind.QUARTERLY, "QUARTERLY")
            .register(FrequencyKind.WEEKLY)
            .register(FrequencyKind.REAL_TIME, "REAL", "REALTIME", "REAL-TIME");


    public FrequencyKindParser() {
        super(defaultAliases, FrequencyKind.class);
    }
}