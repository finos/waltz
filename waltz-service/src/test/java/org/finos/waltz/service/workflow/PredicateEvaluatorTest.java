package org.finos.waltz.service.workflow;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PredicateEvaluatorTest {

    private static final Logger LOG = LoggerFactory.getLogger(PredicateEvaluatorTest.class);

    @Test
    public void evaluate() {

        ImmutableAssessmentContextValue assessmentValue = ImmutableAssessmentContextValue
                .builder()
                .ratingCode("A")
                .ratingName("aRating")
                .ratingExternalId("A_RATING")
                .ratingComment("a rating description")
                .build();

        ImmutableSurveyQuestionResponseContextValue surveyValue = ImmutableSurveyQuestionResponseContextValue
                .builder()
                .value("yes")
                .comment("a survey comment")
                .build();

        ImmutableContextVariable<Object> assessmentVar = ImmutableContextVariable.builder()
                .name("assessmentVar")
                .value(assessmentValue)
                .entityRef(EntityReference.mkRef(EntityKind.APPLICATION, 1L))
                .build();

        ImmutableContextVariable<Object> surveyVar = ImmutableContextVariable.builder()
                .name("surveyVar")
                .value(surveyValue)
                .entityRef(EntityReference.mkRef(EntityKind.APPLICATION, 1L))
                .build();

        Set<ContextVariable<?>> contextVariables = asSet(assessmentVar, surveyVar);

        JexlBuilder builder = new JexlBuilder();
        JexlEngine jexl = builder.create();

        JexlExpression pred1Expr = jexl.createExpression("assessmentVar.ratingCode == 'A'");
        JexlExpression pred2Expr = jexl.createExpression("assessmentVar.ratingCode == 'C' || surveyVar.value == 'yes'");
        JexlExpression pred3Expr = jexl.createExpression("assessmentVar.ratingCode == 'C'");
        JexlExpression invalidExpr = jexl.createExpression("foo?.bar");

        MapContext ctx = createContext(contextVariables);

        assertTrue(evaluatePredicate(pred1Expr, ctx), "pred1Expr should evaluate true, rating equals A");
        assertTrue(evaluatePredicate(pred2Expr, ctx), "pred2Expr should evaluate true, survey value equals 'yes");
        assertFalse(evaluatePredicate(pred3Expr, ctx), "pred3Expr should evaluate false, rating equals A");
        assertFalse(evaluatePredicate(invalidExpr, ctx), "Invalid expressions should return false");

        Object evaluate = invalidExpr.evaluate(ctx);

        System.out.println(evaluate);

    }

    private boolean evaluatePredicate(JexlExpression expression, MapContext ctx) {
        Object result = expression.evaluate(ctx);

        if (result instanceof Boolean) {
            return (Boolean) result;
        } else {
            LOG.warn("Could not evaluate predicate: {}", expression.toString());
            return false;
        }

    }


    private MapContext createContext(Set<ContextVariable<?>> contextVariables) {
        MapContext ctx = new MapContext();

        contextVariables.forEach(ctxVar -> ctx.set(ctxVar.name(), ctxVar.value()));

        return ctx;
    }
}
