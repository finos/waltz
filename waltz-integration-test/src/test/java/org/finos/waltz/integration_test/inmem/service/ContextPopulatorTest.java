package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.ImmutableGenericSelector;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.schema.tables.AssessmentDefinition;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.finos.waltz.schema.tables.RatingScheme;
import org.finos.waltz.schema.tables.RatingSchemeItem;
import org.finos.waltz.schema.tables.records.AssessmentDefinitionRecord;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeItemRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeRecord;
import org.finos.waltz.service.workflow.*;
import org.finos.waltz.test_common_again.helpers.AppHelper;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.String.format;
import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.service.workflow.ContextVariableDeclaration.mkDecl;
import static org.finos.waltz.test_common_again.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ContextPopulatorTest extends BaseInMemoryIntegrationTest {

    private static final AssessmentRating ar = AssessmentRating.ASSESSMENT_RATING;
    private static final RatingScheme rs = RatingScheme.RATING_SCHEME;
    private static final RatingSchemeItem rsi = RatingSchemeItem.RATING_SCHEME_ITEM;
    private static final AssessmentDefinition ad = AssessmentDefinition.ASSESSMENT_DEFINITION;

    private final ImmutableGenericSelector selector = ImmutableGenericSelector.builder()
            .kind(EntityKind.APPLICATION)
            .selector(DSL.select(APPLICATION.ID).from(APPLICATION))
            .build();


    @Autowired
    private ContextPopulator populator;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private DSLContext dsl;


    @Test
    public void fooTest() {

        EntityReference a1 = appHelper.createNewApp(mkName("a1"), ouIds.a);
        EntityReference a2 = appHelper.createNewApp(mkName("a2"), ouIds.a);
        EntityReference a3 = appHelper.createNewApp(mkName("a3"), ouIds.a);

        RatingSchemeRecord scheme = dsl.newRecord(rs);
        scheme.setName(mkName("scheme"));
        scheme.setDescription("desc");
        scheme.store();

        RatingSchemeItemRecord ratingItem1 = mkRatingItem(scheme, "ratingItem1", "X");
        RatingSchemeItemRecord ratingItem2 = mkRatingItem(scheme, "ratingItem2", "Y");

        AssessmentDefinitionRecord def1 = mkAssessmentDef("def1", scheme);
        AssessmentDefinitionRecord def2 = mkAssessmentDef("anotherDef", scheme);

        mkRating(a1, def1, ratingItem1);
        mkRating(a2, def1, ratingItem2);
        mkRating(a1, def2, ratingItem2);

        Set<ContextVariableDeclaration> declarations = SetUtilities.asSet(
                mkDecl("def1Var", EntityKind.ASSESSMENT_DEFINITION, def1.getExternalId()),
                mkDecl("def1VarDupe", EntityKind.ASSESSMENT_DEFINITION, def1.getExternalId()),
                mkDecl("def2Var", EntityKind.ASSESSMENT_DEFINITION, def2.getExternalId()));

        Set<ContextVariable<? extends ContextValue>> vars = populator.populateContext(declarations, selector);

        assertVar(vars, a1, "def1Var", mkAssessmentCodeCheck("X"), "A1");
        assertVar(vars, a1, "def2Var", mkAssessmentCodeCheck("Y"), "A1");
        assertVar(vars, a1, "def1VarDupe", mkAssessmentCodeCheck("X"), "A1");

        assertVar(vars, a2, "def1Var", mkAssessmentCodeCheck("Y"), "A2");
        assertVar(vars, a2, "def1VarDupe", mkAssessmentCodeCheck("Y"), "A2");
    }


    private Predicate<ContextValue> mkAssessmentCodeCheck(String code) {
        return v -> ((AssessmentContextValue) v).getRatingCode().equals(code);
    }


    private RatingSchemeItemRecord mkRatingItem(RatingSchemeRecord scheme,
                                                String name,
                                                String code) {
        RatingSchemeItemRecord rating = dsl.newRecord(rsi);
        rating.setName(mkName(name));
        rating.setSchemeId(scheme.getId());
        rating.setCode(code);
        rating.setDescription("Desc: " + name + " / " + code);
        rating.setColor("red");
        rating.store();
        return rating;
    }


    private AssessmentDefinitionRecord mkAssessmentDef(String defName,
                                                       RatingSchemeRecord scheme) {
        AssessmentDefinitionRecord def = dsl.newRecord(ad);
        String name = mkName(defName);
        String extId = mkName(defName, "extId");
        def.setName(name);
        def.setRatingSchemeId(scheme.getId());
        def.setEntityKind(EntityKind.APPLICATION.name());
        def.setLastUpdatedBy("admin");
        def.setProvenance("waltz");
        def.setExternalId(extId);
        def.store();
        return def;
    }


    private AssessmentRatingRecord mkRating(EntityReference appRef,
                                            AssessmentDefinitionRecord defRecord,
                                            RatingSchemeItemRecord ratingRecord) {
        AssessmentRatingRecord record = dsl.newRecord(ar);
        record.setEntityKind(appRef.kind().name());
        record.setEntityId(appRef.id());
        record.setAssessmentDefinitionId(defRecord.getId());
        record.setRatingId(ratingRecord.getId());
        record.setLastUpdatedBy("admin");
        record.setProvenance("prov");
        record.store();
        return record;
    }


    private void assertVar(Set<ContextVariable<? extends ContextValue>> vars,
                           EntityReference appRef,
                           String varName,
                           Predicate<ContextValue> valueChecker,
                           String refDesc) {
        Optional<ContextVariable<? extends ContextValue>> maybeVar = find(vars, v -> v.entityRef().equals(appRef) && v.name().equals(varName));
        assertTrue(
                maybeVar.isPresent());
        maybeVar.ifPresent(v ->
                assertTrue(valueChecker.test(v.value()),
                        format("Could not find var with name: %s for app: %s", varName, refDesc)));
    }

}
