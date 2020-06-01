package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.ListUtilities.asList;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;

@Service
public class EntityRelationshipsExtractor extends DirectQueryBasedDataExtractor{


    @Autowired
    public EntityRelationshipsExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        registerExtractForCItoMeasurable(mkPath("data-extract", "entity-relationships", "change-initiative", "measurable"));
    }


    private void registerExtractForCItoMeasurable(String path) {
        get(path, (request, response) -> {

            Condition condition = getCondition(request);
            List<Long> involvementKindsIds = getInvolvementKinds(request);

            SelectConditionStep<Record> qry = prepareCiMeasurableQuery(dsl,
                    involvementKindsIds,
                    condition);

            return writeExtract(
                    "ci_measurable_relationships",
                    qry,
                    request,
                    response);
        });
    }

    private Condition getCondition(Request request) {
        Optional<Long> categoryId = getCategoryId(request);
        List<Long> relationshipKindIds = getRelationshipKinds(request);

        Condition categoryCondition = categoryId
                .map(MEASURABLE.MEASURABLE_CATEGORY_ID::eq)
                .orElse(DSL.trueCondition());

        Condition relKindCondition = (relationshipKindIds.isEmpty())
                ? DSL.trueCondition()
                : RELATIONSHIP_KIND.ID.in(relationshipKindIds);

        return categoryCondition.and(relKindCondition);
    }


    private SelectConditionStep<Record> prepareCiMeasurableQuery(DSLContext dsl,
                                                                 List<Long> involvementKinds,
                                                                 Condition condition) {

        Table<Record3<String, Long, String>> involvementsSubQry = dsl
                .select(INVOLVEMENT_KIND.NAME.as("Involvement"),
                        INVOLVEMENT.ENTITY_ID.as("Involved Entity Id"),
                        PERSON.EMAIL.as("Email"))
                .from(INVOLVEMENT)
                .innerJoin(PERSON).on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON.EMPLOYEE_ID))
                .innerJoin(INVOLVEMENT_KIND).on(INVOLVEMENT.KIND_ID.eq(INVOLVEMENT_KIND.ID))
                .where(INVOLVEMENT.KIND_ID.in(involvementKinds))
                .asTable();

        SelectSelectStep<Record> selectFields = dsl
                .selectDistinct(CHANGE_INITIATIVE.ID.as("Change Initiative Id"),
                        CHANGE_INITIATIVE.EXTERNAL_ID.as("Change Initiative External Id"),
                        CHANGE_INITIATIVE.NAME.as("Change Initiative Name"),
                        CHANGE_INITIATIVE.DESCRIPTION.as("Change Initiative Description"),
                        RELATIONSHIP_KIND.NAME.as("Relationship"),
                        MEASURABLE.ID.as("Viewpoint Id"),
                        MEASURABLE.NAME.as("Viewpoint"),
                        MEASURABLE.DESCRIPTION.as("Viewpoint Description"),
                        MEASURABLE.EXTERNAL_ID.as("Viewpoint External Id"))
                .select(involvementsSubQry.field("Involvement", String.class),
                        involvementsSubQry.field("Email", String.class));

        return selectFields
                .from(ENTITY_RELATIONSHIP)
                .innerJoin(CHANGE_INITIATIVE).on(ENTITY_RELATIONSHIP.ID_A.eq(CHANGE_INITIATIVE.ID)
                        .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name())))
                .innerJoin(MEASURABLE).on(ENTITY_RELATIONSHIP.ID_B.eq(MEASURABLE.ID)
                        .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.MEASURABLE.name())))
                .innerJoin(RELATIONSHIP_KIND).on(ENTITY_RELATIONSHIP.RELATIONSHIP.eq(RELATIONSHIP_KIND.CODE))
                .leftJoin(involvementsSubQry).on(CHANGE_INITIATIVE.ID.eq(involvementsSubQry.field("Involved Entity Id", Long.class))
                        .or(MEASURABLE.ID.eq(involvementsSubQry.field("Involved Entity Id", Long.class))))
                .where(condition)
                .and(MEASURABLE.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()));

    }


    private static Optional<Long> getCategoryId(Request request) {
        String categoryId = request.queryParams("category-id");
        return Optional
                .ofNullable(categoryId)
                .map(Long::valueOf);
    }


    private static List<Long> getInvolvementKinds(Request request) {
        String involvementKindList = request.queryParams("inv-kind-ids");
        return (involvementKindList == null)
                ? Collections.emptyList()
                : map(asList(involvementKindList.split(",")), Long::valueOf);
    }


    private static List<Long> getRelationshipKinds(Request request) {
        String relationshipKindList = request.queryParams("rel-kind-ids");
        return (relationshipKindList == null)
                ? Collections.emptyList()
                : map(asList(relationshipKindList.split(",")), Long::valueOf);
    }
}
