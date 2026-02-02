package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.finos.waltz.common.DateTimeUtilities.toSqlDate;
import static org.finos.waltz.schema.Tables.CHANGE_INITIATIVE;

@Service
public class ChangeInitiativeHelper {
    private final DSLContext dsl;

    @Autowired
    public ChangeInitiativeHelper(DSLContext dsl) {
        this.dsl = dsl;
    }

    public long createChangeInitiative(ChangeInitiative ci) {
        return dsl.insertInto(CHANGE_INITIATIVE)
                .set(CHANGE_INITIATIVE.ID, ci.id().get())
                .set(CHANGE_INITIATIVE.EXTERNAL_ID, ci.externalId().get())
                .set(CHANGE_INITIATIVE.NAME, ci.name())
                .set(CHANGE_INITIATIVE.DESCRIPTION, ci.description())
                .set(CHANGE_INITIATIVE.KIND, ci.changeInitiativeKind().name())
                .set(CHANGE_INITIATIVE.LIFECYCLE_PHASE, ci.lifecyclePhase().name())
                .set(CHANGE_INITIATIVE.ORGANISATIONAL_UNIT_ID, ci.organisationalUnitId())
                .set(CHANGE_INITIATIVE.START_DATE, toSqlDate(ci.startDate()))
                .set(CHANGE_INITIATIVE.END_DATE, toSqlDate(ci.endDate()))
                .execute();
    }
}
