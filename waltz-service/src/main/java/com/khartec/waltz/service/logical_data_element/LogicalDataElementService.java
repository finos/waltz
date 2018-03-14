package com.khartec.waltz.service.logical_data_element;

import com.khartec.waltz.data.logical_data_element.LogicalDataElementDao;
import com.khartec.waltz.model.logical_data_element.LogicalDataElement;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class LogicalDataElementService {
    
    private LogicalDataElementDao logicalDataElementDao;


    public LogicalDataElementService(LogicalDataElementDao logicalDataElementDao) {
        checkNotNull(logicalDataElementDao, "logicalDataElementDao cannot be null");
        
        this.logicalDataElementDao = logicalDataElementDao;
    }


    public LogicalDataElement getById(long id) {
        return logicalDataElementDao.getById(id);
    }


    public List<LogicalDataElement> findAll() {
        return logicalDataElementDao.findAll();
    }

}
