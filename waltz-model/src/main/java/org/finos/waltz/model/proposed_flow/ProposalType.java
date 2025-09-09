package org.finos.waltz.model.proposed_flow;

public enum ProposalType {

    CREATE("create");

    private final String prettyName;

    ProposalType(String prettyName){
        this.prettyName = prettyName;
    }

    public String prettyName() {
        return prettyName;
    }
}
