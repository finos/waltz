package org.finos.waltz.service.legal_entity;

import org.finos.waltz.data.legal_entity.LegalEntityRelationshipDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.legal_entity.LegalEntityRelationship;
import org.finos.waltz.schema.tables.records.ChangeLogRecord;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.map;

@Service
public class LegalEntityRelationshipService {

    private final LegalEntityRelationshipDao legalEntityRelationshipDao;
    private static final Logger LOG = LoggerFactory.getLogger(LegalEntityRelationshipService.class);

    @Autowired
    public LegalEntityRelationshipService(LegalEntityRelationshipDao legalEntityRelationshipDao,
                                          DSLContext dsl) {
        checkNotNull(legalEntityRelationshipDao, "legalEntityRelationshipDao cannot be null");

        this.legalEntityRelationshipDao = legalEntityRelationshipDao;
    }


    public Set<LegalEntityRelationship> findByLegalEntityId(long legalEntityId) {
        return legalEntityRelationshipDao.findByLegalEntityId(legalEntityId);
    }

    public Set<LegalEntityRelationship> findByEntityReference(EntityReference ref) {
        return legalEntityRelationshipDao.findByEntityReference(ref);
    }

    public Set<LegalEntityRelationship> findByRelationshipKindId(DSLContext tx, long relKindId) {
        return legalEntityRelationshipDao.findByRelationshipKind(tx, relKindId);
    }

    public Set<LegalEntityRelationship> findByRelationshipKindId(long relationshipKindId) {
        return legalEntityRelationshipDao.findByRelationshipKind(null, relationshipKindId);
    }

    public int bulkAdd(DSLContext tx, Set<LegalEntityRelationship> relationshipsToAdd, String username) {

        mkAdditionChangeLogs(relationshipsToAdd);

        return legalEntityRelationshipDao.bulkAdd(tx, relationshipsToAdd);
    }

    private void mkAdditionChangeLogs(Set<LegalEntityRelationship> relationshipsToAdd) {
        map(relationshipsToAdd, d -> mkChangeLog(d, Operation.ADD));
    }

    private ChangeLogRecord mkChangeLog(LegalEntityRelationship relationship, Operation operation) {

        return null;
    }

    public int bulkUpdate(DSLContext tx, Set<LegalEntityRelationship> relationshipsToUpdate, String username) {
        map(relationshipsToUpdate, d -> mkChangeLog(d, Operation.UPDATE));
        return legalEntityRelationshipDao.bulkUpdate(tx, relationshipsToUpdate);
    }

    public int bulkRemove(DSLContext tx, Set<LegalEntityRelationship> relationships, String username) {
        map(relationships, d -> mkChangeLog(d, Operation.REMOVE));
        return legalEntityRelationshipDao.bulkRemove(tx, relationships);
    }
}
