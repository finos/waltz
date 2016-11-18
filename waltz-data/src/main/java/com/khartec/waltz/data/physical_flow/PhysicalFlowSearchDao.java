package com.khartec.waltz.data.physical_flow;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static com.khartec.waltz.common.StringUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.Actor.ACTOR;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static java.util.Collections.emptyList;


@Repository
public class PhysicalFlowSearchDao {


    private final DSLContext dsl;

    @Autowired
    public PhysicalFlowSearchDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    /**
     * A report is a physical flow which goes to an
     * actor
     * @param query
     * @return
     */
    public List<EntityReference> searchReports(String query) {

        if (isEmpty(query)) {
            return emptyList();
        }

        Field<String> nameField = DSL.concat(
                PHYSICAL_SPECIFICATION.NAME,
                DSL.value(" - "),
                ACTOR.NAME);

        Condition termMatcher = mkTerms(query)
                .stream()
                .reduce(
                        DSL.trueCondition(),
                        (acc, t) -> nameField.like("%" + t + "%"),
                        (acc, t) -> acc.and(t));

        return dsl.select(PHYSICAL_FLOW.ID, nameField)
                .from(PHYSICAL_FLOW)
                .innerJoin(PHYSICAL_SPECIFICATION)
                .on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPECIFICATION.ID))
                .innerJoin(ACTOR)
                .on(PHYSICAL_FLOW.TARGET_ENTITY_ID.eq(ACTOR.ID).and(ACTOR.IS_EXTERNAL.eq(true)))
                .where(PHYSICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.ACTOR.name()))
                .and(termMatcher)
                .fetch()
                .stream()
                .map(r -> EntityReference.mkRef(EntityKind.PHYSICAL_FLOW, r.value1(), r.value2()))
                .collect(Collectors.toList());
    }

}
