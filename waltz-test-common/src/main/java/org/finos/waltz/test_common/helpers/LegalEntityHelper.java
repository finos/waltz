package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.schema.tables.records.LegalEntityRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.LEGAL_ENTITY;

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

}
