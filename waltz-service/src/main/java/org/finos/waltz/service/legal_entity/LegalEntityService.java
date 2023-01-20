package org.finos.waltz.service.legal_entity;

import org.finos.waltz.data.legal_entity.LegalEntityDao;
import org.finos.waltz.data.legal_entity.LegalEntityIdSelectorFactory;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.legal_entity.LegalEntity;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class LegalEntityService {

    private final LegalEntityDao legalEntityDao;

    private final LegalEntityIdSelectorFactory legalEntityIdSelectorFactory = new LegalEntityIdSelectorFactory();

    @Autowired
    public LegalEntityService(LegalEntityDao legalEntityDao) {
        checkNotNull(legalEntityDao, "legalEntityDao cannot be null");
        this.legalEntityDao = legalEntityDao;
    }

    public LegalEntity getById(long id) {
        return legalEntityDao.getById(id);
    }

    public Set<LegalEntity> findBySelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = legalEntityIdSelectorFactory.apply(selectionOptions);
        return legalEntityDao.findBySelector(selector);
    }
}
