package org.finos.waltz.service.legal_entity;

import org.finos.waltz.data.legal_entity.LegalEntityDao;
import org.finos.waltz.data.legal_entity.LegalEntityRelationshipDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.legal_entity.LegalEntityRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class LegalEntityRelationshipService {

    private final LegalEntityRelationshipDao legalEntityRelationshipDao;

    @Autowired
    public LegalEntityRelationshipService(LegalEntityRelationshipDao legalEntityRelationshipDao) {
        checkNotNull(legalEntityRelationshipDao, "legalEntityRelationshipDao cannot be null");
        this.legalEntityRelationshipDao = legalEntityRelationshipDao;
    }


    public Set<LegalEntityRelationship> findByLegalEntityId(long legalEntityId) {
        return legalEntityRelationshipDao.findByLegalEntityId(legalEntityId);
    }

    public Set<LegalEntityRelationship> findByEntityReference(EntityReference ref) {
        return legalEntityRelationshipDao.findByEntityReference(ref);
    }

    public Set<LegalEntityRelationship> findByRelationshipKind(long relKindId) {
        return legalEntityRelationshipDao.findByRelationshipKind(relKindId);
    }
}
