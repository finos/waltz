package com.khartec.waltz.integration_test.inmem.helpers;

import com.khartec.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import com.khartec.waltz.model.datatype.ImmutableDataTypeDecorator;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.rating.AuthoritativenessRatingValue;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.ListUtilities.map;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.LOGICAL_FLOW;

@Service
public class LogicalFlowHelper {

    @Autowired
    private DSLContext dsl;
    
    @Autowired
    private LogicalFlowDao logicalFlowDao;

    @Autowired
    private LogicalFlowDecoratorDao logicalFlowDecoratorDao;


    public LogicalFlow createLogicalFlow(EntityReference refA, EntityReference refB) {
        return logicalFlowDao.addFlow(ImmutableLogicalFlow
                .builder()
                .source(refA)
                .target(refB)
                .lastUpdatedBy("admin")
                .build());
    }


    public void createLogicalFlowDecorators(EntityReference flowRef, Set<Long> dtIds) {

        List<DataTypeDecorator> decorators = map(dtIds, dtId -> ImmutableDataTypeDecorator.builder()
                .rating(AuthoritativenessRatingValue.NO_OPINION)
                .entityReference(flowRef)
                .decoratorEntity(mkRef(EntityKind.DATA_TYPE, dtId))
                .provenance("waltz")
                .lastUpdatedAt(nowUtc())
                .lastUpdatedBy("test")
                .build());

        logicalFlowDecoratorDao.addDecorators(decorators);
    }


    public void clearAllFlows(){
        dsl.deleteFrom(LOGICAL_FLOW).execute();
    }


}
