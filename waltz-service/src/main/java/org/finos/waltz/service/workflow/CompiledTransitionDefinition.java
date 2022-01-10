package org.finos.waltz.service.workflow;

import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

@Value.Immutable
public abstract class CompiledTransitionDefinition implements Predicate<MapContext> {

    private static final Logger LOG = LoggerFactory.getLogger(CompiledTransitionDefinition.class);

    public abstract String name();
    public abstract JexlExpression predicate();
    public abstract String initialState();
    public abstract String targetState();

    public boolean test(MapContext ctx) {
        Object result = predicate().evaluate(ctx);

        if (result instanceof Boolean) {
            return (Boolean) result;
        } else {
            LOG.warn("Could not evaluate predicate: {}", predicate().toString());
            return false;
        }
    }

}
