package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.map;
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

            Optional<Long> categoryId = getCategoryId(request);

            Condition condition = categoryId
                    .map(MEASURABLE.MEASURABLE_CATEGORY_ID::eq)
                    .orElse(DSL.trueCondition());

            Set<Long> involvementKindsIds = getInvolvementKinds(request);

            SelectWhereStep<Record> qry = prepareCiMeasurableQuery(dsl,
                    involvementKindsIds,
                    condition);

            return writeExtract(
                    "entity_relationships_ci_measurable",
                    qry,
                    request,
                    response);
        });
    }


    private SelectWhereStep<Record> prepareCiMeasurableQuery(DSLContext dsl, Set<Long> involvementKinds, Condition condition) {

        SelectConditionStep<Record> involvementsSubQry = dsl
                .select(INVOLVEMENT.KIND_ID.as("Kind Id"))
                .select(INVOLVEMENT_KIND.NAME.as("Involvement"))
                .select(INVOLVEMENT.ENTITY_ID.as("Involved Entity Id"))
                .select(PERSON.EMAIL.as("Email"))
                .from(INVOLVEMENT)
                .innerJoin(PERSON).on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON.EMPLOYEE_ID))
                .innerJoin(INVOLVEMENT_KIND).on(INVOLVEMENT.KIND_ID.eq(INVOLVEMENT_KIND.ID))
                .where(INVOLVEMENT.KIND_ID.in(involvementKinds));

        SelectSelectStep<Record> selectFields = dsl
                .select(CHANGE_INITIATIVE.ID.as("Change Initiative Id"))
                .select(CHANGE_INITIATIVE.EXTERNAL_ID.as("Change Initiative External Id"),
                        CHANGE_INITIATIVE.NAME.as("Change Initiative Name"),
                        CHANGE_INITIATIVE.DESCRIPTION.as("Change Initiative Description"),
                        RELATIONSHIP_KIND.NAME.as("Relationship"),
                        MEASURABLE.ID.as("Viewpoint Id"),
                        MEASURABLE.NAME.as("Viewpoint"),
                        MEASURABLE.DESCRIPTION.as("Viewpoint Description"),
                        MEASURABLE.EXTERNAL_ID.as("Viewpoint External Id"));

        SelectConditionStep<Record> baseQry = selectFields
                .from(ENTITY_RELATIONSHIP)
                .innerJoin(CHANGE_INITIATIVE).on(ENTITY_RELATIONSHIP.ID_A.eq(CHANGE_INITIATIVE.ID)
                        .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name())))
                .innerJoin(MEASURABLE).on(ENTITY_RELATIONSHIP.ID_B.eq(MEASURABLE.ID)
                        .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.MEASURABLE.name())))
                .innerJoin(RELATIONSHIP_KIND).on(ENTITY_RELATIONSHIP.RELATIONSHIP.eq(RELATIONSHIP_KIND.CODE))
                .where(condition)
                .and(MEASURABLE.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()));

        Table<Record> baseTable = baseQry.asTable("baseTable");

        for (Long invId : involvementKinds) {

            Table<Record> joinTable = involvementsSubQry.asTable("joinTable_" + invId.toString()).where();

            Field<Long> ciId = baseTable.field("Change Initiative Id", Long.class);
            Field<Long> mId = baseTable.field("Viewpoint Id", Long.class);
            Field<Long> entityId = joinTable.field("Involved Entity Id", Long.class);

            baseTable = baseTable.leftJoin(joinTable)
                    .on(ciId.eq(entityId).or(mId.eq(entityId))
                            .and(joinTable.field("Kind Id", Long.class).eq(invId)));

        }

        return dsl
                .selectDistinct(baseTable.fields(selectFields.fields()))
                .select(baseTable
                        .fieldStream()
                        .filter(f -> f.getName().equalsIgnoreCase("Email")
                                || f.getName().equalsIgnoreCase("Involvement"))
                        .collect(Collectors.toSet()))
                .from(baseTable);
    }


    private static Optional<Long> getCategoryId(Request request) {
        String categoryId = request.queryParams("category-id");
        return Optional
                .ofNullable(categoryId)
                .map(Long::valueOf);
    }


    private static Set<Long> getInvolvementKinds(Request request) {
        String involvementKindList = request.queryParams("inv-kind-ids");
        return (involvementKindList == null)
                ? Collections.emptySet()
                : map(asSet(involvementKindList.split(",")), Long::valueOf);
    }
}
