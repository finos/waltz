package org.finos.waltz.web.endpoints.extracts.dynamic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableKeyElement.class)
@JsonDeserialize(as = ImmutableKeyElement.class)
public interface KeyElement extends Element{

    String TYPE ="http://waltz.intranet.db.com/types/1/schema#id=KeyElement";

    @Value.Default
    default String type() {
        return TYPE;
    }


    EntityReference key();

}
