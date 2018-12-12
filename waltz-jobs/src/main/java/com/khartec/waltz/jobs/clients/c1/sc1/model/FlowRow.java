package com.khartec.waltz.jobs.clients.c1.sc1.model;

import com.khartec.waltz.jobs.Columns;
import com.khartec.waltz.jobs.clients.c1.sc1.parse.StatusToEntityLifecycleStatus;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.Nullable;
import org.apache.poi.ss.usermodel.Row;
import org.immutables.value.Value;

import static com.khartec.waltz.jobs.XlsUtilities.mapStrCell;
import static com.khartec.waltz.jobs.XlsUtilities.strVal;

@Value.Immutable
public abstract class FlowRow {

    public abstract String sourceAppName();
    public abstract String targetAppName();
    public abstract EntityLifecycleStatus status();


    @Nullable
    public abstract String startDate();

    @Nullable
    public abstract String endDate();


    public static FlowRow fromRow(Row row) {
        ImmutableFlowRow flowRow = ImmutableFlowRow
                .builder()
                .sourceAppName(strVal(row, Columns.A))
                .targetAppName(strVal(row, Columns.F))
                .startDate(strVal(row, Columns.K))
                .endDate(strVal(row, Columns.L))
                .status(mapStrCell(row, Columns.M, StatusToEntityLifecycleStatus::apply))
                .build();

        return flowRow;
    }

}
