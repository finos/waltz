package com.khartec.waltz.jobs.clients.c1.sc1.model;

import com.khartec.waltz.jobs.Columns;
import com.khartec.waltz.jobs.clients.c1.sc1.parse.StatusToLifecyclePhase;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.application.LifecyclePhase;
import org.apache.poi.ss.usermodel.Row;
import org.immutables.value.Value;

import java.util.Date;

import static com.khartec.waltz.jobs.XlsUtilities.mapStrCell;
import static com.khartec.waltz.jobs.XlsUtilities.strVal;

@Value.Immutable
public abstract class ProjectRow {

    public abstract String applicationId();
    public abstract String applicationName();
    public abstract String projectObjectId();
    public abstract String projectName();
    public abstract LifecyclePhase lifecyclePhase();

    @Nullable
    public abstract Date startDate();

    @Nullable
    public abstract Date endDate();


    public static ProjectRow fromRow(Row row) {
        /**
         *
         * A - Anwendungs-ID
           B - Anwendung
           C - Anwendung Version
           D - Projekt Objekt-ID
           E - Projekt
           F - Projekt Status
           G - Projekt Portfoliopflichtig
           H - Projekt Startdatum
           I - Projekt Enddatum
         */
        Date startDate = row.getCell(Columns.H).getDateCellValue();
        Date endDate = row.getCell(Columns.I).getDateCellValue();
        return ImmutableProjectRow
                .builder()
                .applicationId(strVal(row, Columns.A))
                .applicationName(strVal(row, Columns.B))
                .projectObjectId(strVal(row, Columns.D))
                .projectName(strVal(row, Columns.E))
                .lifecyclePhase(mapStrCell(row, Columns.F, StatusToLifecyclePhase::apply))
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

}
