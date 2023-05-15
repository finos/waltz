package org.finos.waltz.jobs.tools.graph;

import org.jgrapht.graph.DefaultEdge;

import java.util.List;

public class DataTypeEdge extends DefaultEdge {

    private final long flowId;
    private final List<String> dataTypes;

    public DataTypeEdge(long flowId, List<String> dataTypes) {
        this.flowId = flowId;
        this.dataTypes = dataTypes;
    }

    protected long getFlowId() {
        return this.flowId;
    }

    protected List<String> getDataTypes() {
        return this.dataTypes;
    }

    @Override
    public String toString() {
        return "(" + this.getSource() + " : " + this.getTarget() + " : [" + String.join(", ", this.dataTypes) + "])";
    }


}
