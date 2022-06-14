package org.finos.waltz.web.endpoints.extracts.dynamic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableValueElement.class)
@JsonDeserialize(as = ImmutableValueElement.class)
public interface ValueElement extends Element{

    String TYPE ="http://waltz.intranet.db.com/types/1/schema#id=ValueElement";

    @Value.Default
    default String type() {
        return TYPE;
    }
    String name();
    String value();
}
