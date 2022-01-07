package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.ImmutableGenericSelector;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.AppHelper;
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
import org.finos.waltz.service.workflow.ContextPopulator;
import org.finos.waltz.service.workflow.ContextVariableDeclaration;
import org.finos.waltz.service.workflow.ContextVariableReference;
import org.finos.waltz.service.workflow.ImmutableContextVariableDeclaration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.schema.tables.Application.APPLICATION;

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

        RatingSchemeRecord scheme = dsl.newRecord(rs);
        scheme.setName(mkName("scheme"));
        scheme.setDescription("desc");
        scheme.store();

        RatingSchemeItemRecord ratingItem1 = dsl.newRecord(rsi);
        ratingItem1.setName(mkName("ratingItem1"));
        ratingItem1.setSchemeId(scheme.getId());
        ratingItem1.setCode("X");
        ratingItem1.setDescription("X Desc");
        ratingItem1.setColor("red");
        ratingItem1.store();

        RatingSchemeItemRecord ratingItem2 = dsl.newRecord(rsi);
        ratingItem2.setName(mkName("ratingItem2"));
        ratingItem2.setSchemeId(scheme.getId());
        ratingItem2.setCode("Y");
        ratingItem2.setDescription("Y Desc");
        ratingItem2.setColor("red");
        ratingItem2.store();

        String defExtId = mkName("defExtId");

        AssessmentDefinitionRecord def = dsl.newRecord(ad);
        def.setName(mkName("def"));
        def.setRatingSchemeId(scheme.getId());
        def.setEntityKind(EntityKind.APPLICATION.name());
        def.setLastUpdatedBy("admin");
        def.setProvenance("waltz");
        def.setExternalId(defExtId);
        def.store();

        AssessmentRatingRecord r1 = dsl.newRecord(ar);
        r1.setEntityKind(a1.kind().name());
        r1.setEntityId(a1.id());
        r1.setAssessmentDefinitionId(def.getId());
        r1.setRatingId(ratingItem1.getId());
        r1.setLastUpdatedBy("admin");
        r1.setProvenance("prov");
        r1.store();

        AssessmentRatingRecord r2 = dsl.newRecord(ar);
        r2.setEntityKind(a2.kind().name());
        r2.setEntityId(a2.id());
        r2.setAssessmentDefinitionId(def.getId());
        r2.setRatingId(ratingItem2.getId());
        r2.setLastUpdatedBy("admin");
        r2.setProvenance("prov");
        r2.store();

        Set<ContextVariableDeclaration> declarations = SetUtilities.asSet(
                ImmutableContextVariableDeclaration
                        .builder()
                        .name("foo")
                        .ref(ContextVariableReference.mkVarRef(EntityKind.ASSESSMENT_DEFINITION, defExtId))
                        .build());

        populator.populateContext(declarations, selector);
    }

}