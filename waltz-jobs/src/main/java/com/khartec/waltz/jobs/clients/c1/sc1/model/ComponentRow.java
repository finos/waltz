package com.khartec.waltz.jobs.clients.c1.sc1.model;

import com.khartec.waltz.jobs.Columns;
import com.khartec.waltz.jobs.clients.c1.sc1.parse.WartungstatusToMaintenanceStatus;
import org.apache.poi.ss.usermodel.Row;
import org.immutables.value.Value;

import static com.khartec.waltz.jobs.XlsUtilities.mapStrCell;
import static com.khartec.waltz.jobs.XlsUtilities.strVal;

@Value.Immutable
public abstract class ComponentRow {

    /*
    0  Anwendungs-ID
    1  Anwendung
    2  Anwendung Version
    3  Anwendung Unternehmenskritisch
    4  Anwendung QS-Datum Technische Architektur
    5  Tier
    6  Layer
    7  Komponente Objekt-ID
    8  Komponente
    9  Komponente Version
    10 Komponente Client-Server-Kennzeichen
    11 Komponente Eigentümer
    12 Komponente Wartungsstatus
    13 Komponente Enddatum
    14 Komponente QS-Datum
    */

    public abstract String tier();
    public abstract String layer();

    public abstract String internalId();
    public abstract String name();
    public abstract String version();
    public abstract String category();
    public abstract String owner();
    public abstract MaintenanceStatus maintenanceStatus();
    public abstract String endDate();
    public abstract String reviewDate();


    public static ComponentRow fromRow(Row row) {
        return ImmutableComponentRow.builder()
                .tier(strVal(row, Columns.F))
                .layer(strVal(row, Columns.G))
                .internalId(strVal(row, Columns.H))
                .name(strVal(row, Columns.I))
                .version(strVal(row, Columns.J))
                .category(strVal(row, Columns.K))
                .owner(strVal(row, Columns.L))
                .maintenanceStatus(mapStrCell(row, Columns.M, WartungstatusToMaintenanceStatus::apply))
                .endDate(strVal(row, Columns.N))
                .reviewDate(strVal(row, Columns.O))
                .build();
    }
}
