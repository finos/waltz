package com.khartec.waltz.service.performance_metric;

import com.khartec.waltz.data.performance_metric.PerformanceMetricPackDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.performance_metric.pack.MetricPack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class PerformanceMetricPackService {

    private final PerformanceMetricPackDao dao;


    @Autowired
    public PerformanceMetricPackService(PerformanceMetricPackDao dao) {
        checkNotNull(dao, "dao cannot be null");
        this.dao = dao;
    }

    public List<EntityReference> findAllPackReferences() {
        return dao.findAllPackReferences();
    }

    public MetricPack getById(long id) {
        return dao.getById(id);
    }

}
