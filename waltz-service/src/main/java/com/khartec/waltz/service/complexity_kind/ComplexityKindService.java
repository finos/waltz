package com.khartec.waltz.service.complexity_kind;

import org.finos.waltz.common.Checks;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.complexity.ComplexityKindDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.complexity.ComplexityKind;
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
