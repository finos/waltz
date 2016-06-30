package com.khartec.waltz.service.data_type_usage;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_type_usage.DataTypeUsageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataTypeUsageService {

    private final DataTypeUsageDao dataTypeUsageDao;
    private final ApplicationIdSelectorFactory selectorFactory;


    @Autowired
    public DataTypeUsageService(DataTypeUsageDao dataTypeUsageDao, ApplicationIdSelectorFactory selectorFactory) {
        Checks.checkNotNull(dataTypeUsageDao, "dataTypeUsageDao cannot be null");
        Checks.checkNotNull(selectorFactory, "selectorFactory cannot be null");
        this.dataTypeUsageDao = dataTypeUsageDao;
        this.selectorFactory = selectorFactory;
    }


    public List<DataTypeUsage> findForIdSelector(EntityKind kind, ApplicationIdSelectionOptions options) {
        return dataTypeUsageDao.findForIdSelector(kind, selectorFactory.apply(options));
    }


    public List<DataTypeUsage> findForEntity(EntityReference ref) {
        return dataTypeUsageDao.findForEntity(ref);
    }


    public List<DataTypeUsage> findForDataType(String dataTypeCode) {
        return dataTypeUsageDao.findForDataType(dataTypeCode);
    }


    public int[] save(List<DataTypeUsage> dataTypeUsages) {
        return dataTypeUsageDao.save(dataTypeUsages);
    }


    public int deleteForEntity(EntityReference ref) {
        return dataTypeUsageDao.deleteForEntity(ref);
    }
}
