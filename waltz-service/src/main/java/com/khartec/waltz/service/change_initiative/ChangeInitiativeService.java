package com.khartec.waltz.service.change_initiative;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ChangeInitiativeService {

    private final ChangeInitiativeDao dao;

    @Autowired
    public ChangeInitiativeService(ChangeInitiativeDao dao) {
        Checks.checkNotNull(dao, "dao cannot be null");
        this.dao = dao;
    }

    public ChangeInitiative getById(Long id) {
        return dao.getById(id);
    }

    public Collection<ChangeInitiative> findForEntityReference(EntityReference ref) {
        return dao.findForEntityReference(ref);
    }

}
