package com.khartec.waltz.service.performance_metric;

import com.khartec.waltz.data.performance_metric.PerformanceMetricDefinitionDao;
import com.khartec.waltz.model.performance_metric.PerformanceMetricDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class PerformanceMetricDefinitionService {

    private final PerformanceMetricDefinitionDao dao;


    @Autowired
    public PerformanceMetricDefinitionService(PerformanceMetricDefinitionDao dao) {
        checkNotNull(dao, "dao cannot be null");
        this.dao = dao;
    }


    public Collection<PerformanceMetricDefinition> findAll() {
        return dao.findAll();
    }


    public PerformanceMetricDefinition getById(long id) {
        return dao.getById(id);
    }

}
