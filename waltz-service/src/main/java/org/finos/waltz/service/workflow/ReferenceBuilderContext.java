package org.finos.waltz.service.workflow;

import org.apache.commons.jexl3.JexlContext;
import org.finos.waltz.common.SetUtilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReferenceBuilderContext implements JexlContext {

    private Map<String, ContextVariableReference> referenceMap = new HashMap<>();

    @Override
    public Object get(String name) {
        return referenceMap.get(name);
    }

    @Override
    public void set(String name, Object value) {
        referenceMap.put(name, (ContextVariableReference) value);
    }

    public Set<String> keys() {
        return referenceMap.keySet();
    }

    @Override
    public boolean has(String name) {
        return referenceMap.containsKey(name);
    }

    public Set<ContextVariableDeclaration> declarations() {
        return SetUtilities.map(
                referenceMap.entrySet(),
                kv -> ImmutableContextVariableDeclaration.builder()
                    .name(kv.getKey())
                    .ref(kv.getValue())
                    .build());
    }
}
