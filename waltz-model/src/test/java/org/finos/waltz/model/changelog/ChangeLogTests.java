package org.finos.waltz.model.changelog;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeLogTests {

    private static final ChangeLog base = ImmutableChangeLog.builder()
            .parentReference(mkRef(EntityKind.ORG_UNIT, 1L))
            .message("test")
            .userId("test")
            .operation(Operation.UNKNOWN)
            .build();


    @Test
    public void childRefUnavailableIfMissingId() {
        ChangeLog missingId = ImmutableChangeLog
                .copyOf(base)
                .withChildKind(EntityKind.APPLICATION);

        assertEquals(
                Optional.empty(),
                missingId.childRef(),
                "Should not be able to derive child ref since child id is not present");
    }


    @Test
    public void childRefUnavailableIfMissingKind() {
        ChangeLog missingKind = ImmutableChangeLog
                .copyOf(base)
                .withChildId(99L);

        assertEquals(
                Optional.empty(),
                missingKind.childRef(),
                "Should not be able to derive child ref since child kind is not present");
    }


    @Test
    public void childRefAvailableIfKindAndIdProvided() {
        ChangeLog withIdAndKind = ImmutableChangeLog
                .copyOf(base)
                .withChildKind(EntityKind.APPLICATION)
                .withChildId(99L);

        assertEquals(
                Optional.of(mkRef(EntityKind.APPLICATION, 99L)),
                withIdAndKind.childRef(),
                "Child ref should be available as kind and id were provided");
    }
}
