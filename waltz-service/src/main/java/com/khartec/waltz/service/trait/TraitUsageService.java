package com.khartec.waltz.service.trait;

import com.khartec.waltz.data.trait.TraitUsageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.trait.TraitUsage;
import com.khartec.waltz.model.trait.TraitUsageKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraitUsageService {

    private final TraitUsageDao dao;


    @Autowired
    public TraitUsageService(TraitUsageDao dao) {
        this.dao = dao;
    }


    public List<TraitUsage> findAll() {
        return dao.findAll();
    }


    public List<TraitUsage> findByEntityKind(EntityKind kind) {
        return dao.findByEntityKind(kind);
    }

    public List<TraitUsage> findByTraitId(long id) {
        return dao.findByTraitId(id);
    }

    public List<TraitUsage> findByEntityReference(EntityReference reference) {
        return dao.findByEntityReference(reference);
    }

    public List<TraitUsage> addTraitUsage(EntityReference entityReference, Long traitId) {
        dao.addTraitUsage(entityReference, traitId, TraitUsageKind.EXHIBITS);
        return findByEntityReference(entityReference);
    }

    public List<TraitUsage> removeTraitUsage(EntityReference entityReference, long traitId) {
        dao.removeTraitUsage(entityReference, traitId);
        return findByEntityReference(entityReference);
    }
}
