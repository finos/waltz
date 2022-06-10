package org.finos.waltz.web.endpoints.extracts.dynamic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangeInitiativeSchemaTest {

    private ImmutableChangeInitiativeSchema changeInitiativeSchema =
            ImmutableChangeInitiativeSchema.builder().id("id")
                    .name("dummy")
                    .phase("dummy-phase")
                    .build();
    @Test
    public void canCreateChangeInitiativeWithCorrectURItype(){
        assertEquals(ChangeInitiativeSchema.TYPE,changeInitiativeSchema.type());
    }

    @Test
    public void warnIfURItypeIsChanged(){
        String expectedType ="http://waltz.intranet.db.com/types/1/schema#id=change-initiative";
        assertEquals(expectedType,changeInitiativeSchema.type(),
                "Consumers using Jackson serialisation may be broken if you change type as it forms part of public API");
    }
}