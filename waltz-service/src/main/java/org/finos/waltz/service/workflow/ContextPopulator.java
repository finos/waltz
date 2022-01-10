package org.finos.waltz.service.workflow;

import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.schema.tables.AssessmentDefinition;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.finos.waltz.schema.tables.RatingSchemeItem;
import org.jooq.DSLContext;
import org.jooq.Record5;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.map;

@Service
public class ContextPopulator {

    private static final AssessmentRating ar = AssessmentRating.ASSESSMENT_RATING;
    private static final RatingSchemeItem rsi = RatingSchemeItem.RATING_SCHEME_ITEM;
    private static final AssessmentDefinition ad = AssessmentDefinition.ASSESSMENT_DEFINITION;

    private final DSLContext dsl;

    @Autowired
    public ContextPopulator(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<ContextVariable<String>> populateContext(Set<ContextVariableDeclaration> declarations,
                                                        GenericSelector selector)
    {
        Map<EntityKind, Collection<ContextVariableDeclaration>> declarationsByRefKind = groupBy(declarations, d -> d.ref().kind());

        Set<ContextVariable<String>> assessmentVariables = fetchAssessmentVariables(
                declarationsByRefKind.get(EntityKind.ASSESSMENT_DEFINITION),
                selector);

        return assessmentVariables;

    }

    private Set<ContextVariable<String>> fetchAssessmentVariables(Collection<ContextVariableDeclaration> declarations,
                                                                  GenericSelector selector) {
        if (isEmpty(declarations)) {
            return emptySet();
        } else {
            Map<String, Collection<String>> extIdsToVarNames = MapUtilities.groupBy(
                    declarations,
                    d -> d.ref().externalId(),
                    ContextVariableDeclaration::name);

            return dsl
                .fetch(prepareAssessmentQuery(declarations, selector))
                .stream()
                .flatMap(r -> extIdsToVarNames
                        .getOrDefault(r.get(ad.EXTERNAL_ID), Collections.emptySet())
                        .stream()
                        .map(varName -> ImmutableContextVariable
                            .<String>builder()
                            .name(varName)
                            .value(r.get(rsi.CODE))
                            .entityRef(EntityReference.mkRef(selector.kind(), r.get(ar.ENTITY_ID)))
                            .build()))
                .collect(Collectors.toSet());
        }
    }


    private Select<Record5<String, String, String, String, Long>> prepareAssessmentQuery(
            Collection<ContextVariableDeclaration> declarations,
            GenericSelector genericSelector)
    {
        Set<String> defExtIds = map(
                declarations,
                d -> d.ref().externalId());
        return DSL
                .select(rsi.EXTERNAL_ID,
                        rsi.NAME,
                        rsi.CODE,
                        ad.EXTERNAL_ID,
                        ar.ENTITY_ID)
                .from(ar)
                .innerJoin(rsi).on(rsi.ID.eq(ar.RATING_ID))
                .innerJoin(ad).on(ad.ID.eq(ar.ASSESSMENT_DEFINITION_ID))
                .where(ad.EXTERNAL_ID.in(defExtIds))
                .and(ar.ENTITY_KIND.eq(genericSelector.kind().name()))
                .and(ar.ENTITY_ID.in(genericSelector.selector()));
    }

}
