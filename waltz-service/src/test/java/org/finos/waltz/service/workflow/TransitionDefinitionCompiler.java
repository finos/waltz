package org.finos.waltz.service.workflow;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;

import java.util.List;

import static org.finos.waltz.common.ListUtilities.map;

public class TransitionDefinitionCompiler {


    public static List<CompiledTransitionDefinition> compile(List<TransitionDefinition> defns) {

        JexlBuilder builder = new JexlBuilder();
        JexlEngine jexl = builder.create();

        return map(defns, d -> d.compile(jexl));

    }
}
