package org.finos.waltz.integration_test.inmem.story;

import org.finos.waltz.data.data_type.DataTypeDao;
import org.finos.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.AppHelper;
import org.finos.waltz.integration_test.inmem.helpers.DataTypeHelper;
import org.finos.waltz.integration_test.inmem.helpers.LogicalFlowHelper;
import org.finos.waltz.integration_test.inmem.helpers.UserHelper;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.model.utils.IdUtilities.toIds;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SuggestedDataTypesTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private LogicalFlowHelper logicalFlowHelper;

    @Autowired
    private DataTypeHelper dataTypeHelper;

    @Autowired
    private LogicalFlowDecoratorDao decoratorDao;

    @Autowired
    private DataTypeDao dataTypeDao;

    @Test
    public void dataTypeSuggestionsShowAllActiveDataTypesAssociatedToTheGivenEntity() {
        Long dt1 = dataTypeHelper.createDataType(mkName("dt1"));
        Long dt2 = dataTypeHelper.createDataType(mkName("dt2"));
        Long dt3 = dataTypeHelper.createDataType(mkName("dt3"));

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.b);
        EntityReference c = appHelper.createNewApp(mkName("c"), ouIds.a);
        EntityReference d = appHelper.createNewApp(mkName("d"), ouIds.a);

        LogicalFlow ab = logicalFlowHelper.createLogicalFlow(a, b);
        LogicalFlow bc = logicalFlowHelper.createLogicalFlow(b, c);
        LogicalFlow bd = logicalFlowHelper.createLogicalFlow(b, d);

        logicalFlowHelper.createLogicalFlowDecorators(ab.entityReference(), asSet(dt1, dt2));
        logicalFlowHelper.createLogicalFlowDecorators(bc.entityReference(), asSet(dt2));
        logicalFlowHelper.createLogicalFlowDecorators(bd.entityReference(), asSet(dt3));

        // This sets up a flow structure which can be visually depicted like:
        //
        // a --[1,2]-> b
        //             b --[2]-> c
        //             b --[3]-> d

        List<DataType> suggestedDataTypes = dataTypeDao.findSuggestedByEntityRef(b);

        assertEquals(
                asSet(dt1, dt2, dt3),
                toIds(suggestedDataTypes),
                "App B should have dt1, dt2 and dt3");

        // drop bd, leading to a diagram like:
        //
        // a --[1,2]-> b
        //             b --[2]-> c

        logicalFlowHelper.removeFlow(bd.id().get());

        assertEquals(
                asSet(dt1, dt2),
                toIds(suggestedDataTypes),
                "App B should now have dt1, dt2. The type: dt3 should no longer be present as it was associated to a removed flow");

    }
}
