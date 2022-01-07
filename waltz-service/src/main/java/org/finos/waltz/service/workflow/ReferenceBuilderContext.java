package org.finos.waltz.service.workflow;

import org.apache.commons.jexl3.JexlContext;
import org.finos.waltz.common.SetUtilities;
import org.jooq.lambda.tuple.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jooq.lambda.tuple.Tuple.tuple;

public class ReferenceBuilderContext implements JexlContext {

    private Map<String, WorkflowContextVariableReference> referenceMap = new HashMap<>();

    @Override
    public Object get(String name) {
        return referenceMap.get(name);
    }

    @Override
    public void set(String name, Object value) {
        referenceMap.put(name, (WorkflowContextVariableReference) value);
    }

    public Set<String> keys() {
        return referenceMap.keySet();
    }

    @Override
    public boolean has(String name) {
        return referenceMap.containsKey(name);
    }

    public Set<WorkflowContextVariableDeclaration> declarations() {
        return SetUtilities.map(
                referenceMap.entrySet(),
                kv -> ImmutableWorkflowContextVariableDeclaration.builder()
                    .name(kv.getKey())
                    .ref(kv.getValue())
                    .build());
    }
}
