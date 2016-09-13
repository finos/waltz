package com.khartec.waltz.model;


import java.time.LocalDate;
import java.util.Date;

import static com.khartec.waltz.common.DateTimeUtilities.toLocalDate;

public enum EndOfLifeStatus {

    END_OF_LIFE,
    NOT_END_OF_LIFE;


    public static EndOfLifeStatus calculateEndOfLifeStatus(Date eolDate) {
        if (eolDate != null
                && toLocalDate(eolDate).isBefore(LocalDate.now())) {
            return END_OF_LIFE;
        }
        return NOT_END_OF_LIFE;
    }
}
