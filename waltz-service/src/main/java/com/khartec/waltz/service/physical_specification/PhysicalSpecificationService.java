package com.khartec.waltz.service.physical_specification;

import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.ProduceConsumeGroup;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PhysicalSpecificationService {

    private final PhysicalSpecificationDao specificationDao;
    private final PhysicalSpecificationSelectorFactory selectorFactory;


    @Autowired
    public PhysicalSpecificationService(PhysicalSpecificationDao specificationDao,
                                        PhysicalSpecificationSelectorFactory selectorFactory)
    {
        checkNotNull(specificationDao, "specificationDao cannot be null");
        checkNotNull(selectorFactory, "selectorFactory cannot be null");
        this.specificationDao = specificationDao;
        this.selectorFactory = selectorFactory;
    }


    public ProduceConsumeGroup<PhysicalSpecification> findByAppId(long id) {
        return specificationDao.findByAppId(id);
    }


    public List<PhysicalSpecification> findByProducerAppId(long id) {
        return specificationDao.findByProducerAppId(id);
    }


    public Collection<PhysicalSpecification> findByConsumerAppId(long id) {
        return specificationDao.findByConsumerAppId(id);
    }


    public PhysicalSpecification getById(long id) {
        return specificationDao.getById(id);
    }

    public Collection<PhysicalSpecification> findBySelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = selectorFactory.apply(options);
        return specificationDao.findBySelector(selector);

    }
}
