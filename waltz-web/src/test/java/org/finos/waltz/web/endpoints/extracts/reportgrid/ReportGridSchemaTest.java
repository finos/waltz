package org.finos.waltz.web.endpoints.extracts.reportgrid;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.web.json.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportGridSchemaTest {

    private final String TEST_NAME = "er-name";
    private final EntityReference TEST_REF = ImmutableEntityReference.mkRef(EntityKind.COMPLEXITY,1L,TEST_NAME);
    private final KeyCell KEY_CELL = KeyCell.fromRef(TEST_REF);

    private ImmutableReportGridSchema reportGridSchema =
            ImmutableReportGridSchema.builder().id("id")
                    .apiTypes(ImmutableApiTypes.builder().build())
                    .name("dummy")
                    .grid(ImmutableGrid.builder().build())
                    .build();


    @Test
    public void rowCellsArrayIsInitialised(){
         Row row = ImmutableRow.builder()
                 .id(KEY_CELL)
                 .build();
        assertEquals( 0, row.cells().size());
    }

    @Test
    public void keyCellCopiesValuesFromEntityRef(){
        Row row = ImmutableRow.builder()
                .id(KeyCell.fromRef(TEST_REF))
                .build();
        assertEquals( 0, row.cells().size());
        assertTrue(row.id().name().isPresent() && row.id().name().get().equals(TEST_NAME));
        assertEquals(EntityKind.COMPLEXITY,row.id().kind());
    }


    @Test
    public void canCreateChangeInitiativeWithCorrectURItype(){
        assertEquals(ReportGridSchema.TYPE, reportGridSchema.type());
    }


    @Test
    public void warnIfURItypeIsChanged(){
        String expectedType ="/types/1/schema#id=report-grid";
        assertEquals(expectedType, reportGridSchema.type(),
                "Consumers using Jackson serialisation may be broken if you change type as it forms part of public API");
    }
}