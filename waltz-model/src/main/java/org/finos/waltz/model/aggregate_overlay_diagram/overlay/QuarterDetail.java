package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.Quarter;
import org.immutables.value.Value;

import java.time.LocalDate;

import static java.lang.String.format;
import static org.finos.waltz.model.Quarter.fromInt;

@Value.Immutable
@JsonSerialize(as = ImmutableQuarterDetail.class)
public abstract class QuarterDetail {

    public abstract int year();
    public abstract int quarter();

    @Value.Derived
    public Quarter quarterName(){
       return fromInt(quarter());
    }
    @Value.Derived
    public int additionalQuarters() {
        int currentYear = DateTimeUtilities.today().getYear();
        int additionalYears = year() - currentYear;
        return additionalYears * 4 + quarter();
    };

    public static QuarterDetail mkQuarterDetail(LocalDate date) {
        return ImmutableQuarterDetail
                .builder()
                .quarter(lookupQuarter(date.getMonthValue()))
                .year(date.getYear())
                .build();
    }

    public static Integer lookupQuarter(int monthValue) {
        switch (monthValue) {
            case 1:
            case 2:
            case 3:
                return 1;
            case 4:
            case 5:
            case 6:
                return 2;
            case 7:
            case 8:
            case 9:
                return 3;
            case 10:
            case 11:
            case 12:
                return 4;
            default:
                throw new IllegalArgumentException(format("Cannot parse quarter for a month value [%d] that is not between 1 and 12", monthValue));
        }
    }
}