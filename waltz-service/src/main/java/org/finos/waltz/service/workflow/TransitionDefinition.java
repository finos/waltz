package org.finos.waltz.service.workflow;

import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

@Value.Immutable
public abstract class TransitionDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(TransitionDefinition.class);

    public abstract String name();
    public abstract String predicate();
    public abstract String initialState();
    public abstract String targetState();


    public CompiledTransitionDefinition compile(JexlEngine jexl) {
        try {
            JexlExpression expression = jexl.createExpression(predicate());

            return ImmutableCompiledTransitionDefinition.builder()
                    .name(name())
                    .predicate(expression)
                    .initialState(initialState())
                    .targetState(targetState())
                    .build();
        } catch (Exception e) {
            String msg = format("Unable to compile transition definition: [%s] with predicate: [%s]", name(), predicate());
            LOG.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }

    }

}
