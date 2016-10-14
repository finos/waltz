package com.khartec.waltz.service.involvement_kind;

import com.khartec.waltz.data.involvement_kind.InvolvementKindDao;
import com.khartec.waltz.model.invovement_kind.InvolvementKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class InvolvementKindService {

    private final InvolvementKindDao involvementKindDao;


    @Autowired
    public InvolvementKindService(InvolvementKindDao involvementKindDao) {
        checkNotNull(involvementKindDao, "involvementKindDao cannot be null");

        this.involvementKindDao = involvementKindDao;
    }


    public List<InvolvementKind> findAll() {
        return involvementKindDao.findAll();
    }


    public InvolvementKind getById(long id) {
        return involvementKindDao.getById(id);
    }


    public InvolvementKind create(InvolvementKind involvementKind) {
        checkNotNull(involvementKind, "involvementKind cannot be null");

        return involvementKindDao.create(involvementKind);
    }


    public InvolvementKind update(InvolvementKind involvementKind) {
        checkNotNull(involvementKind, "involvementKind cannot be null");

        return involvementKindDao.update(involvementKind);
    }


    public boolean delete(long id) {
        return involvementKindDao.deleteIfNotUsed(id);
    }

}
