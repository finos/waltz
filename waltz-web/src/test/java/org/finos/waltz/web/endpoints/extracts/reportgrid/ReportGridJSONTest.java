package org.finos.waltz.web.endpoints.extracts.reportgrid;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.report_grid.ImmutableReportSubject;
import org.finos.waltz.model.report_grid.ReportSubject;
import org.finos.waltz.web.json.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportGridJSONTest {

    private final String TEST_NAME = "er-name";

    private final EntityReference TEST_REF = ImmutableEntityReference.mkRef(EntityKind.COMPLEXITY, 1L, TEST_NAME);

    private final ReportSubject TEST_SUBJECT = ImmutableReportSubject.builder()
            .entityReference(TEST_REF)
            .lifecyclePhase(LifecyclePhase.PRODUCTION)
            .build();

    private final KeyCell KEY_CELL = KeyCell.fromSubject(TEST_SUBJECT);

    private final ReportGridJSON reportGridJSON =
            ImmutableReportGridJSON.builder()
                    .id("id")
                    .apiTypes(ImmutableApiTypes.builder().build())
                    .name("dummy")
                    .grid(ImmutableGrid.builder().build())
                    .build();


    @Test
    void rowCellsArrayIsInitialised(){
         Row row = ImmutableRow.builder()
                 .id(KEY_CELL)
                 .build();
        assertEquals( 0, row.cells().size());
    }

    @Test
    void keyCellCopiesValuesFromEntityRef(){
        Row row = ImmutableRow.builder()
                .id(KeyCell.fromSubject(TEST_SUBJECT))
                .build();
        assertEquals( 0, row.cells().size());
        assertEquals(ApiTypes.KEYCELL, row.id().type());
    }


    @Test
    void canCreateChangeInitiativeWithCorrectURI(){
        assertEquals(ReportGridJSON.REPORT_GRID_TYPE, reportGridJSON.type());
    }


    @Test
    void warnIfURIisChanged(){
        String expectedType ="/types/1/schema#id=report-grid";
        assertEquals(expectedType, reportGridJSON.type(),
                "Consumers using Jackson serialisation may be broken if you change type as it forms part of public API");
    }
}