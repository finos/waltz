package com.khartec.waltz.service.complexity;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.complexity.ComplexityDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.complexity.Complexity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ComplexityService {


    private final ComplexityDao complexityDao;

    @Autowired
    ComplexityService(ComplexityDao complexityDao) {
        Checks.checkNotNull(complexityDao, "complexityDao cannot be null");
        this.complexityDao = complexityDao;
    }


    public Set<Complexity> findByEntityReference(EntityReference ref){
        return complexityDao.findByEntityReference(ref);
    }

}
