package org.finos.waltz.data.permission;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.permission.ImmutablePermissionViewItem;
import org.finos.waltz.model.permission.PermissionViewItem;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.AssessmentDefinition;
import org.finos.waltz.schema.tables.InvolvementGroup;
import org.finos.waltz.schema.tables.InvolvementGroupEntry;
import org.finos.waltz.schema.tables.InvolvementKind;
import org.finos.waltz.schema.tables.MeasurableCategory;
import org.finos.waltz.schema.tables.PermissionGroup;
import org.finos.waltz.schema.tables.PermissionGroupInvolvement;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.data.JooqUtilities.maybeReadRef;
import static org.finos.waltz.model.EntityReference.mkRef;

@Repository
public class PermissionViewDao {

    private static final InvolvementGroup ig = Tables.INVOLVEMENT_GROUP.as("ig");
    private static final InvolvementGroupEntry ige = Tables.INVOLVEMENT_GROUP_ENTRY.as("ige");
    private static final InvolvementKind ik = Tables.INVOLVEMENT_KIND.as("ik");
    private static final PermissionGroup pg = Tables.PERMISSION_GROUP.as("pg");
    private static final PermissionGroupInvolvement pgi = Tables.PERMISSION_GROUP_INVOLVEMENT.as("pgi");
    private static final MeasurableCategory mc = Tables.MEASURABLE_CATEGORY.as("mc");
    private static final AssessmentDefinition ad = Tables.ASSESSMENT_DEFINITION.as("ad");

    private final DSLContext dsl;

    @Autowired
    public PermissionViewDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Set<PermissionViewItem> findAll() {
        Field<String> qualifierName = DSL
                .coalesce(mc.NAME, ad.NAME, null)
                .as("qualifier_name");

        return dsl
                .select(pg.NAME, pg.ID, pg.EXTERNAL_ID, pg.DESCRIPTION,
                        pgi.PARENT_KIND, pgi.SUBJECT_KIND, pgi.QUALIFIER_KIND, pgi.QUALIFIER_ID, qualifierName, pgi.OPERATION,
                        ig.NAME, ig.ID, ig.EXTERNAL_ID,
                        ik.NAME, ik.DESCRIPTION, ik.EXTERNAL_ID, ik.ID)
                .from(ig)
                .innerJoin(pgi).on(pgi.INVOLVEMENT_GROUP_ID.eq(ig.ID))
                .innerJoin(ige).on(ig.ID.eq(ige.INVOLVEMENT_GROUP_ID))
                .innerJoin(ik).on(ik.ID.eq(ige.INVOLVEMENT_KIND_ID))
                .innerJoin(pg).on(pg.ID.eq(pgi.PERMISSION_GROUP_ID))
                .leftJoin(mc).on(pgi.QUALIFIER_KIND.eq(EntityKind.MEASURABLE_CATEGORY.name()).and(mc.ID.eq(pgi.QUALIFIER_ID)))
                .leftJoin(ad).on(pgi.QUALIFIER_KIND.eq(EntityKind.ASSESSMENT_DEFINITION.name()).and(ad.ID.eq(pgi.QUALIFIER_ID)))
                .fetchSet(r -> ImmutablePermissionViewItem
                        .builder()
                        .parentKind(EntityKind.valueOf(r.get(pgi.PARENT_KIND)))
                        .subjectKind(EntityKind.valueOf(r.get(pgi.SUBJECT_KIND)))
                        .qualifier(maybeReadRef(r, pgi.QUALIFIER_KIND, pgi.QUALIFIER_ID, qualifierName).orElse(null))
                        .operation(Operation.valueOf(r.get(pgi.OPERATION)))
                        .permissionGroup(mkRef(EntityKind.PERMISSION_GROUP, r.get(pg.ID), r.get(pg.NAME), r.get(pg.DESCRIPTION), r.get(pg.EXTERNAL_ID)))
                        .involvementGroup(mkRef(EntityKind.INVOLVEMENT_GROUP, r.get(ig.ID), r.get(ig.NAME), null, r.get(ig.EXTERNAL_ID)))
                        .involvementKind(mkRef(EntityKind.INVOLVEMENT_KIND, r.get(ik.ID), r.get(ik.NAME), r.get(ik.DESCRIPTION), r.get(ik.EXTERNAL_ID)))
                        .build());
    }

}
