package org.finos.waltz.service.legal_entity;

import org.finos.waltz.data.legal_entity.LegalEntityRelationshipKindDao;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class LegalEntityRelationshipKindService {

    private final LegalEntityRelationshipKindDao legalEntityRelationshipKindDao;

    @Autowired
    public LegalEntityRelationshipKindService(LegalEntityRelationshipKindDao legalEntityRelationshipKindDao) {
        checkNotNull(legalEntityRelationshipKindDao, "legalEntityRelationshipKindDao cannot be null");
        this.legalEntityRelationshipKindDao = legalEntityRelationshipKindDao;
    }

    public LegalEntityRelationshipKind getById(long id) {
        return legalEntityRelationshipKindDao.getById(id);
    }

    public Set<LegalEntityRelationshipKind> findAll() {
        return legalEntityRelationshipKindDao.findAll();
    }
}
