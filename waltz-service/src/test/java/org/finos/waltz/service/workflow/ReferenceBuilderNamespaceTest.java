package org.finos.waltz.service.workflow;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.finos.waltz.model.EntityKind;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.IOUtilities.getFileResource;
import static org.finos.waltz.common.IOUtilities.readAsString;
import static org.finos.waltz.common.MapUtilities.newHashMap;
import static org.finos.waltz.service.workflow.ContextVariableReference.mkVarRef;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReferenceBuilderNamespaceTest {


    private final JexlBuilder builder = new JexlBuilder();
    private final JexlEngine jexl = builder
            .namespaces(newHashMap(null, new ReferenceBuilderNamespace()))
            .create();

    @Test
    public void canMakeAssessmentRefs() {
        JexlExpression expr = jexl.createExpression("assessment('ARCH_REVIEW')");
        assertEquals(mkVarRef(EntityKind.ASSESSMENT_DEFINITION, "ARCH_REVIEW"), expr.evaluate(null));
    }


    @Test
    public void evalTest() {
        JexlBuilder builder = new JexlBuilder();
        JexlEngine jexl = builder.create();

        JexlExpression expr = jexl.createExpression("x == 'IN_SCOPE' && y == 10");

        MapContext ctx1 = new MapContext();
        ctx1.set("x", "IN_SCOPE");
        ctx1.set("y", 10);

        MapContext ctx2 = new MapContext();
        ctx2.set("x", "IN_SCOPE");
        ctx2.set("y", 12);

        System.out.printf("Result1: [%s]\n", expr.evaluate(ctx1));
        System.out.printf("Result2: [%s]\n", expr.evaluate(ctx2));
    }


    @Test
    public void declarationsFromFile() throws IOException {

        String declarationExpressions = readAsString(getFileResource("decl-test.cfg").getInputStream());

        ReferenceBuilderContext jexlCtx = new ReferenceBuilderContext();

        JexlExpression expr = jexl.createExpression(declarationExpressions);
        expr.evaluate(jexlCtx);

        Set<ContextVariableDeclaration> declarations = jexlCtx.declarations();

        assertEquals(3, declarations.size());

        Optional<ContextVariableDeclaration> x = find(declarations, t -> t.name().equals("x"));
        Optional<ContextVariableDeclaration> y = find(declarations, t -> t.name().equals("y"));
        Optional<ContextVariableDeclaration> longName = find(declarations, t -> t.name().equals("longName"));

        assertTrue(x.isPresent());
        assertTrue(y.isPresent());
        assertTrue(longName.isPresent());

        x.ifPresent(t -> assertEquals(t.ref(), ReferenceBuilderNamespace.assessment("wibble")));
        y.ifPresent(t -> assertEquals(t.ref(), ReferenceBuilderNamespace.surveyQuestionResponse("surv", "q1")));
        longName.ifPresent(t -> assertEquals(t.ref(), ReferenceBuilderNamespace.surveyQuestionResponse("surv", "q2")));
    }
}
