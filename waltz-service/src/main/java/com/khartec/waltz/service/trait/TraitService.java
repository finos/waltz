package com.khartec.waltz.service.trait;


import com.khartec.waltz.data.trait.TraitDao;
import com.khartec.waltz.model.trait.Trait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraitService {

    private final TraitDao dao;


    @Autowired
    public TraitService(TraitDao dao) {
        this.dao = dao;
    }


    public List<Trait> findAll() {
        return dao.findAll();
    }


    public Trait getById(long id) {
        return dao.getById(id);
    }


    public List<Trait> findByIds(List<Long> ids) {
        return dao.findByIds(ids);
    }

    public List<Trait> findApplicationDeclarableTraits() {
        return dao.findApplicationDeclarableTraits();
    }

}
