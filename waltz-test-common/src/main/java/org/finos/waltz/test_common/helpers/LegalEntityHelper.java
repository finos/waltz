package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.schema.tables.records.LegalEntityRecord;
import org.finos.waltz.schema.tables.records.LegalEntityRelationshipKindRecord;
import org.finos.waltz.schema.tables.records.LegalEntityRelationshipRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.*;

@Service
public class LegalEntityHelper {

    @Autowired
    private DSLContext dsl;

    public EntityReference create(String name) {
        LegalEntityRecord r = dsl.newRecord(LEGAL_ENTITY);
        r.setName(name);
        r.setDescription(name);
        r.setExternalId(name);
        r.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE.name());
        r.setLastUpdatedBy(name);
        r.store();
        return mkRef(EntityKind.LEGAL_ENTITY, r.getId());
    }


    public long createLegalEntityRelationshipKind(String name) {
        LegalEntityRelationshipKindRecord r = dsl.newRecord(LEGAL_ENTITY_RELATIONSHIP_KIND);
        r.setName(name);
        r.setDescription(name);
        r.setExternalId(name);
        r.setCardinality(Cardinality.ZERO_MANY.name());
        r.setLastUpdatedBy(name);
        r.setTargetKind(EntityKind.APPLICATION.name());
        r.store();
        return r.getId();
    }


    public long createLegalEntityRelationship(EntityReference targetRef, EntityReference legalEntityRef, long relKindId) {
        LegalEntityRelationshipRecord r = dsl.newRecord(LEGAL_ENTITY_RELATIONSHIP);
        r.setLegalEntityId(legalEntityRef.id());
        r.setTargetKind(targetRef.kind().name());
        r.setTargetId(targetRef.id());
        r.setRelationshipKindId(relKindId);
        r.setLastUpdatedBy("test");
        r.setTargetKind(EntityKind.APPLICATION.name());
        r.store();
        return r.getId();
    }

}
