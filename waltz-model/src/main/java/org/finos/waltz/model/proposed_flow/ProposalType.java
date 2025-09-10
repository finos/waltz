package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)   // force object shape
@JsonDeserialize(using = ProposalTypeJsonDeserializer.class) // fall-back for string
public enum ProposalType {

    CREATE("Create"),
    DELETE("Delete"),
    EDIT("Edit");

    private final String prettyName;

    ProposalType(String prettyName) {
        this.prettyName = prettyName;
    }

    @JsonProperty("key")          // becomes "key" in JSON
    public String getKey() {
        return name();
    }

    @JsonProperty("value")        // becomes "value" in JSON
    public String getValue() {
        return prettyName;
    }

    /* helper for deserializer */
    @JsonCreator
    public static ProposalType fromKey(String key) {
        return key == null ? null : ProposalType.valueOf(key);
    }
}
