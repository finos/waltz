package com.khartec.waltz.service.complexity_kind;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.complexity.ComplexityKindDao;
import com.khartec.waltz.model.complexity.ComplexityKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ComplexityKindService {

    private final ComplexityKindDao complexityKindDao;

    @Autowired
    ComplexityKindService(ComplexityKindDao complexityKindDao){
        Checks.checkNotNull(complexityKindDao, "complexityKindDao must not be null.");
        this.complexityKindDao = complexityKindDao;
    }


    public Set<ComplexityKind> findAll(){
        return complexityKindDao.findAll();
    }

}
