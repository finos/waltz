package org.finos.waltz.web.endpoints.extracts.dynamic;

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
        String expectedType ="http://waltz.intranet.db.com/types/1/schema#id=report-grid";
        assertEquals(expectedType, reportGridSchema.type(),
                "Consumers using Jackson serialisation may be broken if you change type as it forms part of public API");
    }
}