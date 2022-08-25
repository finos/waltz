package org.finos.waltz.integration_test.inmem.story;

import org.finos.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.service.data_type.DataTypeService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.DataTypeHelper;
import org.finos.waltz.test_common.helpers.LogicalFlowHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.utils.IdUtilities.toIds;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SuggestedDataTypesTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private LogicalFlowHelper logicalFlowHelper;

    @Autowired
    private DataTypeHelper dataTypeHelper;

    @Autowired
    private LogicalFlowDecoratorDao decoratorDao;

    @Autowired
    private DataTypeService dataTypeService;

    @Test
    public void dataTypeSuggestionsShowAllActiveDataTypesAssociatedToTheGivenEntity() {
        Long dt1 = dataTypeHelper.createDataType(mkName("dt1"));
        Long dt2 = dataTypeHelper.createDataType(mkName("dt2"));
        Long dt3 = dataTypeHelper.createDataType(mkName("dt3"));
        Long dt4 = dataTypeHelper.createDataType(mkName("dt3"));

        EntityReference a = appHelper.createNewApp(mkName("a"), ouIds.a);
        EntityReference b = appHelper.createNewApp(mkName("b"), ouIds.b);
        EntityReference c = appHelper.createNewApp(mkName("c"), ouIds.a);
        EntityReference d = appHelper.createNewApp(mkName("d"), ouIds.a);
        EntityReference e = appHelper.createNewApp(mkName("e"), ouIds.a);  // not part of any flow
        EntityReference f = appHelper.createNewApp(mkName("f"), ouIds.a);

        LogicalFlow aToB = logicalFlowHelper.createLogicalFlow(a, b);
        LogicalFlow bToC = logicalFlowHelper.createLogicalFlow(b, c);
        LogicalFlow bToD = logicalFlowHelper.createLogicalFlow(b, d);
        LogicalFlow cToF = logicalFlowHelper.createLogicalFlow(c, f);

        logicalFlowHelper.createLogicalFlowDecorators(aToB.entityReference(), asSet(dt1, dt2));
        logicalFlowHelper.createLogicalFlowDecorators(bToC.entityReference(), asSet(dt2));
        logicalFlowHelper.createLogicalFlowDecorators(bToD.entityReference(), asSet(dt3));
        logicalFlowHelper.createLogicalFlowDecorators(cToF.entityReference(), asSet(dt4));

        // This sets up a flow structure which can be visually depicted like:
        //             *
        // a --[1,2]-> b
        //             b --[2]-> c
        //                       c --[4]-> d
        //             b --[3]-> d

        assertEquals(
                asSet(dt1, dt2, dt3),
                toIds(dataTypeService.findSuggestedByEntityRef(b)),
                "App B should have dt1, dt2 and dt3");

        assertTrue(
                dataTypeService.findSuggestedByEntityRef(e).isEmpty(),
                "App E should have no data type suggestions as it has no flows");

        // drop bToD, leading to a diagram like:
        //             *
        // a --[1,2]-> b
        //             b --[2]-> c
        //                       c --[4]-> d
        int rmRc = logicalFlowHelper.removeFlow(bToD.id().get());
        assertEquals(1, rmRc, "flow bToD should have been removed");

        assertEquals(
                asSet(dt1, dt2),
                toIds(dataTypeService.findSuggestedByEntityRef(b)),
                "App B should now have dt1, dt2. The type: dt3 should no longer be present as it was associated to a removed flow (bToD)");
    }
}
