package org.finos.waltz.test_common.helpers;

import org.finos.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.ImmutableDataTypeDecorator;
import org.finos.waltz.model.logical_flow.ImmutableLogicalFlow;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.LOGICAL_FLOW;

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

    public List<DataTypeDecorator> fetchDecoratorsForFlow(Long flowId) {
        return logicalFlowDecoratorDao.findByEntityId(flowId);
    }


    public void clearAllFlows() {
        dsl.deleteFrom(LOGICAL_FLOW).execute();
    }


    public int removeFlow(Long flowId) {
        return logicalFlowDao.removeFlow(flowId, "admin");
    }

    public void makeReadOnly(long flowId) {
        dsl
                .update(LOGICAL_FLOW)
                .set(LOGICAL_FLOW.IS_READONLY, true)
                .where(LOGICAL_FLOW.ID.eq(flowId))
                .execute();
    }
}
