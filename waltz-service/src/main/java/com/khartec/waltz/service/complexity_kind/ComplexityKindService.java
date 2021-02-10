package com.khartec.waltz.service.complexity_kind;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.complexity.ComplexityKindDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.complexity.ComplexityKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ComplexityKindService {

    private final ComplexityKindDao complexityKindDao;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Autowired
    ComplexityKindService(ComplexityKindDao complexityKindDao){
        Checks.checkNotNull(complexityKindDao, "complexityKindDao must not be null.");
        this.complexityKindDao = complexityKindDao;
    }


    public Set<ComplexityKind> findAll(){
        return complexityKindDao.findAll();
    }


    public ComplexityKind getById(Long complexityKindId){
        return complexityKindDao.getById(complexityKindId);
    }


    public Set<ComplexityKind> findBySelector(EntityKind targetKind, IdSelectionOptions selectionOptions) {
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, selectionOptions);

        return complexityKindDao.findBySelector(genericSelector);
    }
}
