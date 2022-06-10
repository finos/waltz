package org.finos.waltz.web.endpoints.extracts.dynamic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableChangeInitiativeSchema.class)
@JsonDeserialize(as = ImmutableChangeInitiativeSchema.class)
public interface ChangeInitiativeSchema extends Schema{

    String TYPE ="http://waltz.intranet.db.com/types/1/schema#id=change-initiative";

    @Value.Default
    default String type() {
        return TYPE;
    }
    String id();
    String name();
    String phase();
}