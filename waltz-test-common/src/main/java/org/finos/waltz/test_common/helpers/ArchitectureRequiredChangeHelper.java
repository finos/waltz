package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.architecture_required_change.ArchitectureRequiredChange;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.DateTimeUtilities.toSqlDate;
import static org.finos.waltz.common.DateTimeUtilities.toSqlTimestamp;
import static org.finos.waltz.schema.Tables.ARCHITECTURE_REQUIRED_CHANGE;

@Service
public class ArchitectureRequiredChangeHelper {
    private final DSLContext dsl;

    @Autowired
    public ArchitectureRequiredChangeHelper(DSLContext dsl) {
        this.dsl = dsl;
    }

    public long createArchitectureRequiredChange(ArchitectureRequiredChange arc) {
        return dsl.insertInto(ARCHITECTURE_REQUIRED_CHANGE)
                .set(ARCHITECTURE_REQUIRED_CHANGE.ID, arc.id())
                .set(ARCHITECTURE_REQUIRED_CHANGE.EXTERNAL_ID, arc.externalId())
                .set(ARCHITECTURE_REQUIRED_CHANGE.TITLE, arc.title())
                .set(ARCHITECTURE_REQUIRED_CHANGE.DESCRIPTION, arc.description())
                .set(ARCHITECTURE_REQUIRED_CHANGE.STATUS, arc.status())
                .set(ARCHITECTURE_REQUIRED_CHANGE.LINKED_ENTITY_ID, arc.linkedEntityId().orElse(null))
                .set(ARCHITECTURE_REQUIRED_CHANGE.LINKED_ENTITY_KIND, arc.linkedEntityKind().map(EntityKind::name).orElse(null))
                .set(ARCHITECTURE_REQUIRED_CHANGE.EXTERNAL_PARENT_ID, arc.externalParentId().orElse(null))
                .set(ARCHITECTURE_REQUIRED_CHANGE.CREATED_AT, toSqlTimestamp(arc.createdAt()))
                .set(ARCHITECTURE_REQUIRED_CHANGE.CREATED_BY, arc.createdBy())
                .set(ARCHITECTURE_REQUIRED_CHANGE.UPDATED_AT, toSqlTimestamp(arc.updatedAt()))
                .set(ARCHITECTURE_REQUIRED_CHANGE.UPDATED_BY, arc.updatedBy())
                .set(ARCHITECTURE_REQUIRED_CHANGE.PROVENANCE, arc.provenance().get())
                .set(ARCHITECTURE_REQUIRED_CHANGE.ENTITY_LIFECYCLE_STATUS, arc.entityLifecycleStatus().name())
                .set(ARCHITECTURE_REQUIRED_CHANGE.MILESTONE_RAG, arc.milestoneRag().orElse(null))
                .set(ARCHITECTURE_REQUIRED_CHANGE.MILESTONE_FORECAST_DATE, toSqlTimestamp(arc.milestoneForecastDate().orElse(null)))
                .execute();
    }

}
