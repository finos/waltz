package com.khartec.waltz.service.logical_data_element;

import com.khartec.waltz.data.logical_data_element.LogicalDataElementDao;
import com.khartec.waltz.data.logical_data_element.LogicalDataElementIdSelectorFactory;
import com.khartec.waltz.data.logical_data_element.search.LogicalDataElementSearchDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.logical_data_element.LogicalDataElement;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class LogicalDataElementService {
    
    private final LogicalDataElementDao logicalDataElementDao;
    private final LogicalDataElementSearchDao logicalDataElementSearchDao;
    private final LogicalDataElementIdSelectorFactory idSelectorFactory;


    public LogicalDataElementService(LogicalDataElementDao logicalDataElementDao,
                                     LogicalDataElementSearchDao logicalDataElementSearchDao,
                                     LogicalDataElementIdSelectorFactory logicalDataElementIdSelectorFactory) {
        checkNotNull(logicalDataElementDao, "logicalDataElementDao cannot be null");
        checkNotNull(logicalDataElementSearchDao, "logicalDataElementSearchDao cannot be null");
        checkNotNull(logicalDataElementIdSelectorFactory, "logicalDataElementIdSelectorFactory cannot be null");

        this.logicalDataElementDao = logicalDataElementDao;
        this.logicalDataElementSearchDao = logicalDataElementSearchDao;
        this.idSelectorFactory = logicalDataElementIdSelectorFactory;
    }


    public LogicalDataElement getById(long id) {
        return logicalDataElementDao.getById(id);
    }


    public LogicalDataElement getByExternalId(String externalId) {
        return logicalDataElementDao.getByExternalId(externalId);
    }


    public List<LogicalDataElement> findAll() {
        return logicalDataElementDao.findAll();
    }


    public List<LogicalDataElement> findBySelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = idSelectorFactory.apply(options);
        return logicalDataElementDao.findBySelector(selector);
    }


    public List<LogicalDataElement> search(String query, EntitySearchOptions options) {
        return logicalDataElementSearchDao.search(query, options);
    }

}
