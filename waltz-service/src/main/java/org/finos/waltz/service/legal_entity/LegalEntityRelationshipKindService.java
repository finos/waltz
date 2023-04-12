package org.finos.waltz.service.legal_entity;

import org.finos.waltz.data.legal_entity.LegalEntityRelationshipIdSelectorFactory;
import org.finos.waltz.data.legal_entity.LegalEntityRelationshipKindDao;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.legal_entity.LegalEntityRelKindStat;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipKind;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class LegalEntityRelationshipKindService {

    private final LegalEntityRelationshipKindDao legalEntityRelationshipKindDao;
    private final LegalEntityRelationshipIdSelectorFactory legalEntityRelationshipIdSelectorFactory = new LegalEntityRelationshipIdSelectorFactory();

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

    public Set<LegalEntityRelKindStat> findUsageStats() {
        return legalEntityRelationshipKindDao.findUsageStats();
    }

    public LegalEntityRelKindStat findUsageStatsByKindAndSelector(Long relKindId,
                                                                  IdSelectionOptions opts) {

        Select<Record1<Long>> relSelector = legalEntityRelationshipIdSelectorFactory.apply(opts);
        return legalEntityRelationshipKindDao.findUsageStatsByKindAndSelector(relKindId, relSelector);
    }
}
