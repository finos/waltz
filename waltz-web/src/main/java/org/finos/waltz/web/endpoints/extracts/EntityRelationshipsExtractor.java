package org.finos.waltz.web.endpoints.extracts;

import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.CommonTableFields;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.schema.tables.ApplicationGroup;
import org.finos.waltz.schema.tables.ChangeInitiative;
import org.finos.waltz.schema.tables.EntityRelationship;
import org.finos.waltz.schema.tables.Involvement;
import org.finos.waltz.schema.tables.InvolvementKind;
import org.finos.waltz.schema.tables.Measurable;
import org.finos.waltz.schema.tables.Person;
import org.finos.waltz.schema.tables.RelationshipKind;
import org.finos.waltz.web.WebUtilities;
import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Record5;
import org.jooq.Record9;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static org.finos.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.finos.waltz.schema.tables.Measurable.MEASURABLE;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;

@Service
public class EntityRelationshipsExtractor extends DirectQueryBasedDataExtractor{


    private static final InvolvementKind ik = INVOLVEMENT_KIND;
    private static final Person p = PERSON;
    private static final Involvement inv = INVOLVEMENT;
    private static final RelationshipKind rk = RELATIONSHIP_KIND;
    private static final EntityRelationship er = ENTITY_RELATIONSHIP;
    private static final Measurable m = MEASURABLE;
    private static final ChangeInitiative ci = CHANGE_INITIATIVE;
    private static final ApplicationGroup ag = APPLICATION_GROUP;


    @Autowired
    public EntityRelationshipsExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        registerExtractForCItoMeasurable(mkPath("data-extract", "entity-relationships", "change-initiative", "measurable"));
        get(mkPath("data-extract", "entity-relationships", "kind", ":kind", "id", ":id"), this::handleExtractForEntity);
    }


    private Object handleExtractForEntity(Request request, Response response) throws IOException {
        EntityReference refFromParams = WebUtilities.getEntityReference(request);

        CommonTableFields<?> common = JooqUtilities.determineCommonTableFields(refFromParams.kind());

        Record2<String, String> selfEntity = dsl
                .select(common.nameField(),
                        common.externalIdField())
                .from(common.table())
                .where(common.idField().eq(refFromParams.id()))
                .fetchOne();

        ImmutableEntityReference ref = ImmutableEntityReference.copyOf(refFromParams)
                .withName(selfEntity.get(common.nameField()))
                .withDescription(selfEntity.get(common.externalIdField()));

        SelectConditionStep<Record5<Long, String, String, String, String>> fromRef = DSL
                .select(er.ID_B.as("id"),
                        er.KIND_B.as("kind"),
                        er.DESCRIPTION,
                        rk.CODE.as("rel_kind"),
                        rk.NAME.as("rel_name"))
                .from(er)
                .innerJoin(rk).on(rk.CODE.eq(er.RELATIONSHIP))
                .where(er.KIND_A.eq(ref.kind().name())
                        .and(er.ID_A.eq(ref.id())));

        SelectConditionStep<Record5<Long, String, String, String, String>> toRef = DSL
                .select(er.ID_A.as("id"),
                        er.KIND_A.as("kind"),
                        er.DESCRIPTION,
                        rk.CODE.as("rel_kind"),
                        rk.REVERSE_NAME.as("rel_name"))
                .from(er)
                .innerJoin(rk).on(rk.CODE.eq(er.RELATIONSHIP))
                .where(er.KIND_B.eq(ref.kind().name())
                        .and(er.ID_B.eq(ref.id())));

        CommonTableExpression<Record5<Long, String, String, String, String>> rels = DSL
                .name("rels")
                .fields("id", "kind", "description", "rel_kind", "rel_name")
                .as(fromRef.union(toRef));

        Select<Record9<Long, String, String, String, String, Long, String, String, String>> qry = dsl
                .with(rels)
                .select(DSL.val(ref.id()).as("self_id"),
                        DSL.val(ref.name().orElse("?")).as("self_name"),
                        DSL.val(ref.externalId().orElse("?")).as("self_external_id"),
                        rels.field("rel_kind", String.class).as("relationship_kind"),
                        rels.field("rel_name", String.class).as("relationship_name"),
                        rels.field("id", Long.class).as("other_id"),
                        rels.field("kind", String.class).as("other_kind"),
                        DSL.coalesce(m.NAME, ci.NAME, ag.NAME).as("other_name"),
                        DSL.coalesce(m.EXTERNAL_ID, ci.EXTERNAL_ID, ag.EXTERNAL_ID).as("other_external_id"))
                .from(rels)
                .leftJoin(m)
                .on(m.ID.eq(rels.field("id", Long.class))
                        .and(rels.field("kind", String.class).eq(EntityKind.MEASURABLE.name())))
                .leftJoin(ci)
                .on(ci.ID.eq(rels.field("id", Long.class))
                        .and(rels.field("kind", String.class).eq(EntityKind.CHANGE_INITIATIVE.name())))
                .leftJoin(ag)
                .on(ag.ID.eq(rels.field("id", Long.class))
                        .and(rels.field("kind", String.class).eq(EntityKind.APP_GROUP.name())));

        return writeExtract(
                "entity_relationships",
                qry,
                request,
                response);
    }


    private void registerExtractForCItoMeasurable(String path) {
        get(path, (request, response) -> {

            Condition condition = getCondition(request);
            List<Long> involvementKindsIds = getInvolvementKinds(request);

            //returns all of kindA, with kindB if exists
            SelectOnConditionStep<Record> qry = prepareCiMeasurableQuery(dsl,
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
                .map(m.MEASURABLE_CATEGORY_ID::eq)
                .orElse(DSL.trueCondition());

        Condition relKindCondition = (relationshipKindIds.isEmpty())
                ? DSL.trueCondition()
                : rk.ID.in(relationshipKindIds);

        return categoryCondition.and(relKindCondition);
    }


    private SelectOnConditionStep<Record> prepareCiMeasurableQuery(DSLContext dsl,
                                                                   List<Long> involvementKinds,
                                                                   Condition condition) {

        Table<Record3<String, Long, String>> involvementsSubQry = dsl
                .select(ik.NAME.as("Involvement"),
                        inv.ENTITY_ID.as("Involved Entity Id"),
                        p.EMAIL.as("Email"))
                .from(inv)
                .innerJoin(p).on(inv.EMPLOYEE_ID.eq(p.EMPLOYEE_ID))
                .innerJoin(ik).on(inv.KIND_ID.eq(ik.ID))
                .where(inv.KIND_ID.in(involvementKinds))
                .asTable();

        Table<Record> entityRelationshipsSubQry = DSL
                .select(rk.NAME.as("Relationship"),
                        rk.DESCRIPTION.as("Relationship Kind Description"),
                        er.ID_A.as("Id A"),
                        er.KIND_A.as("Kind A"),
                        er.DESCRIPTION.as("Relationship Description"),
                        m.ID.as("Viewpoint Id"),
                        m.NAME.as("Viewpoint"),
                        m.DESCRIPTION.as("Viewpoint Description"),
                        m.EXTERNAL_ID.as("Viewpoint External Id"))
                .select(involvementsSubQry.field("Involvement", String.class),
                        involvementsSubQry.field("Email", String.class))
                .from(er)
                .innerJoin(m).on(er.ID_B.eq(m.ID)
                        .and(er.KIND_B.eq(EntityKind.MEASURABLE.name())))
                .innerJoin(rk).on(er.RELATIONSHIP.eq(rk.CODE))
                .leftJoin(involvementsSubQry).on(er.ID_A.eq(involvementsSubQry.field("Involved Entity Id", Long.class))
                        .or(m.ID.eq(involvementsSubQry.field("Involved Entity Id", Long.class))))
                .where(condition)
                .and(m.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))
                .and(er.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .asTable();

        SelectSelectStep<Record> selectFields = dsl
                .selectDistinct(
                        ci.ID.as("Change Initiative Id"),
                        ci.EXTERNAL_ID.as("Change Initiative External Id"),
                        ci.KIND.as("Change Initiative Kind"),
                        ci.NAME.as("Change Initiative Name"),
                        ci.DESCRIPTION.as("Change Initiative Description"))
                .select(entityRelationshipsSubQry.field("Relationship", String.class),
                        entityRelationshipsSubQry.field("Relationship Kind Description", String.class),
                        entityRelationshipsSubQry.field("Relationship Description", String.class),
                        entityRelationshipsSubQry.field("Viewpoint Id", String.class),
                        entityRelationshipsSubQry.field("Viewpoint", String.class),
                        entityRelationshipsSubQry.field("Viewpoint Description", String.class),
                        entityRelationshipsSubQry.field("Viewpoint External Id", String.class),
                        entityRelationshipsSubQry.field("Involvement", String.class),
                        entityRelationshipsSubQry.field("Email", String.class));

        return selectFields
                .from(ci)
                .leftJoin(entityRelationshipsSubQry)
                .on(ci.ID.eq(entityRelationshipsSubQry.field("Id A", Long.class)));

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
