/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.data.attestation.AttestationPreCheckDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.AppGroupHelper;
import org.finos.waltz.integration_test.inmem.helpers.DataTypeHelper;
import org.finos.waltz.integration_test.inmem.helpers.LogicalFlowHelper;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.service.attestation.AttestationPreCheckService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static org.finos.waltz.schema.tables.DataType.DATA_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AttestationPreCheckServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private LogicalFlowService lfSvc;

    @Autowired
    private LogicalFlowHelper lfHelper;

    @Autowired
    private AttestationPreCheckService aipcSvc;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private DataTypeHelper dataTypeHelper;

    @Autowired
    private AppGroupHelper appGroupHelper;


    @Test
    public void notAllowedToAttestAttestIfNoFlows() {
        EntityReference appRef = mkNewAppRef();
        List<String> result = aipcSvc.calcLogicalFlowPreCheckFailures(appRef);
        assertFalse(result.isEmpty());
    }


    @Test
    public void notAllowedToAttestAttestIfUnknownIncomingDataTypeFlows() {
        EntityReference aRef = mkNewAppRef();
        EntityReference bRef = mkNewAppRef();

        // create flow with unknown datatype
        long unkId = dataTypeHelper.createUnknownDatatype();
        LogicalFlow flow = lfHelper.createLogicalFlow(aRef, bRef);
        lfHelper.createLogicalFlowDecorators(flow.entityReference(), asSet(unkId));

        List<String> aResult = aipcSvc.calcLogicalFlowPreCheckFailures(aRef);
        assertTrue("ok as unknown is outgoing", aResult.isEmpty());

        List<String> bResult = aipcSvc.calcLogicalFlowPreCheckFailures(bRef);
        assertFalse("should fail as unknown is incoming", bResult.isEmpty());
    }


    @Test
    public void notAllowedToAttestAttestIfDeprecatedIncomingDataTypeFlows() {
        EntityReference aRef = mkNewAppRef();
        EntityReference bRef = mkNewAppRef();

        // create flow with deprecated datatype
        long deprecatedTypeId = createDeprecatedDataType();
        LogicalFlow flow = lfHelper.createLogicalFlow(aRef, bRef);
        lfHelper.createLogicalFlowDecorators(flow.entityReference(), asSet(deprecatedTypeId));

        List<String> aResult = aipcSvc.calcLogicalFlowPreCheckFailures(aRef);
        assertTrue("ok as deprecated is outgoing", aResult.isEmpty());

        List<String> bResult = aipcSvc.calcLogicalFlowPreCheckFailures(bRef);
        assertFalse("should fail as deprecated is incoming", bResult.isEmpty());
    }


    @Test
    public void allowedToAttestAttestIfInExemptionGroupAndDeprecatedIncomingDataTypeFlows() throws InsufficientPrivelegeException {
        EntityReference aRef = mkNewAppRef();
        EntityReference bRef = mkNewAppRef();

        // create flow with unknown datatype
        long deprecatedId = createDeprecatedDataType();
        LogicalFlow flow = lfHelper.createLogicalFlow(aRef, bRef);
        lfHelper.createLogicalFlowDecorators(flow.entityReference(), asSet(deprecatedId));

        createGroupWithApps(
                AttestationPreCheckDao.GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_DEPRECATED_DATA_TYPE_CHECK,
                bRef);

        List<String> bResult = aipcSvc.calcLogicalFlowPreCheckFailures(bRef);
        assertTrue("should pass as target app is in exemption from deprecated flows group", bResult.isEmpty());
    }


    @Test
    public void allowedToAttestAttestIfInExemptionGroupAndUnknownIncomingDataTypeFlows() throws InsufficientPrivelegeException {
        EntityReference aRef = mkNewAppRef();
        EntityReference bRef = mkNewAppRef();

        // create flow with unknown datatype
        long unkId = dataTypeHelper.createUnknownDatatype();
        LogicalFlow flow = lfHelper.createLogicalFlow(aRef, bRef);
        lfHelper.createLogicalFlowDecorators(flow.entityReference(), asSet(unkId));

        createGroupWithApps(
                AttestationPreCheckDao.GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_UNKNOWN_DATA_TYPE_CHECK,
                bRef);

        List<String> bResult = aipcSvc.calcLogicalFlowPreCheckFailures(bRef);
        assertTrue("should pass as target app is in exemption from unknown flows group", bResult.isEmpty());
    }


    @Test
    public void allowedToAttestIfInExemptionGroupAndNoFlows() throws InsufficientPrivelegeException {
        EntityReference appRef = mkNewAppRef();

        createGroupWithApps(
                AttestationPreCheckDao.GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_FLOW_COUNT_CHECK,
                appRef);

        List<String> result = aipcSvc.calcLogicalFlowPreCheckFailures(appRef);

        assertTrue("should be allowed to attest as in the no-flows exemption group", result.isEmpty());
    }


    private void createGroupWithApps(String extId, EntityReference appRef) throws InsufficientPrivelegeException {
        Long groupId = appGroupHelper.createAppGroupWithAppRefs(mkName(extId), asSet(appRef));

        dsl.update(APPLICATION_GROUP)
                .set(APPLICATION_GROUP.EXTERNAL_ID, extId)
                .where(APPLICATION_GROUP.ID.eq(groupId))
                .execute();
    }


    private long createDeprecatedDataType() {
        long deprecatedTypeId = counter.incrementAndGet();
        dataTypeHelper.createDataType(deprecatedTypeId, mkName("deprecated"), mkName("deprecated"));
        dsl.update(DATA_TYPE).set(DATA_TYPE.DEPRECATED, true).where(DATA_TYPE.ID.eq(deprecatedTypeId)).execute();
        return deprecatedTypeId;
    }


    @AfterEach
    public void removeExemptionGroups() {
        dsl.deleteFrom(Tables.APPLICATION_GROUP)
                .where(APPLICATION_GROUP.EXTERNAL_ID.eq(AttestationPreCheckDao.GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_FLOW_COUNT_CHECK))
                .execute();
    }

}