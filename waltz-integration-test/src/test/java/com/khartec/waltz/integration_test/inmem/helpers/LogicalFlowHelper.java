package com.khartec.waltz.integration_test.inmem.helpers;

import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogicalFlowHelper {

    private final LogicalFlowDao logicalFlowDao;

    @Autowired
    public LogicalFlowHelper(LogicalFlowDao logicalFlowDao) {
        this.logicalFlowDao = logicalFlowDao;
    }


    public LogicalFlow createLogicalFlow(EntityReference refA, EntityReference refB) {
        return logicalFlowDao.addFlow(ImmutableLogicalFlow
                .builder()
                .source(refA)
                .target(refB)
                .lastUpdatedBy("admin")
                .build());
    }

}
