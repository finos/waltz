package org.finos.waltz.service.legal_entity;

import org.finos.waltz.data.legal_entity.LegalEntityDao;
import org.finos.waltz.model.legal_entity.LegalEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class LegalEntityService {

    private final LegalEntityDao legalEntityDao;

    @Autowired
    public LegalEntityService(LegalEntityDao legalEntityDao) {
        checkNotNull(legalEntityDao, "legalEntityDao cannot be null");
        this.legalEntityDao = legalEntityDao;
    }

    public LegalEntity getById(long id) {
        return legalEntityDao.getById(id);
    }
}
