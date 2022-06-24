package org.finos.waltz.web.endpoints.extracts.reportgrid;

import org.finos.waltz.web.json.ImmutableApiTypes;
import org.finos.waltz.web.json.ImmutableGrid;
import org.finos.waltz.web.json.ImmutableReportGridSchema;
import org.finos.waltz.web.json.ReportGridSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportGridSchemaTest {

    private ImmutableReportGridSchema reportGridSchema =
            ImmutableReportGridSchema.builder().id("id")
                    .apiTypes(ImmutableApiTypes.builder().build())
                    .name("dummy")
                    .grid(ImmutableGrid.builder().build())
                    .build();


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