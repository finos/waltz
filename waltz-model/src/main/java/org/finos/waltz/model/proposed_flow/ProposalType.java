package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.annotation.JsonValue;
public enum ProposalType {

    CREATE("Create"),
    DELETE("Delete"),
    EDIT("Edit");

    private final String prettyName;

    ProposalType(String prettyName) {
        this.prettyName = prettyName;
    }

    @JsonValue
    public String getKey() {
        return name();
    }
}
