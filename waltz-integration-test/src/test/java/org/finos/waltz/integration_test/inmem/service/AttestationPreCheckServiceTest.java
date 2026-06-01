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

import static org.finos.waltz.schema.Tables.MEASURABLE_CATEGORY;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;
import static org.finos.waltz.schema.Tables.MEASURABLE;
import static org.finos.waltz.schema.Tables.SETTINGS;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.data.attestation.AttestationPreCheckDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.service.attestation.AttestationPreCheckService;
import org.finos.waltz.test_common.helpers.AppGroupHelper;
import org.finos.waltz.test_common.helpers.DataTypeHelper;
import org.finos.waltz.test_common.helpers.LogicalFlowHelper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static org.finos.waltz.schema.tables.DataType.DATA_TYPE;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.*;

@Disabled("Problem with H2")
public class AttestationPreCheckServiceTest extends BaseInMemoryIntegrationTest {

    private static final String ATTESTATION_PRECHECK_PRIMARY_FLAG_KEY = "ATTESTATION_PRECHECK_PRIMARY_FLAG" ;
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

    private static final long TEST_MEASURABLE_CATEGORY_ID = 33L;
    private final EntityReference TEST_ENTITY_REF = mkNewAppRef();



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

        aipcSvc.calcLogicalFlowPreCheckFailures(aRef);
        List<String> aResult = aipcSvc.calcLogicalFlowPreCheckFailures(aRef);
        assertTrue(aResult.isEmpty(), "ok as unknown is outgoing");

        List<String> bResult = aipcSvc.calcLogicalFlowPreCheckFailures(bRef);
        assertFalse(bResult.isEmpty(), "should fail as unknown is incoming");
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
        assertEquals(Collections.emptyList(), aResult, "should have no failure messages as deprecated is outgoing");

        List<String> bResult = aipcSvc.calcLogicalFlowPreCheckFailures(bRef);
        assertFalse(bResult.isEmpty(), "should fail as deprecated is incoming");
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
        assertTrue(bResult.isEmpty(), "should pass as target app is in exemption from deprecated flows group");
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
        assertTrue(bResult.isEmpty(), "should pass as target app is in exemption from unknown flows group");
    }


    @Test
    public void allowedToAttestIfInExemptionGroupAndNoFlows() throws InsufficientPrivelegeException {
        EntityReference appRef = mkNewAppRef();

        createGroupWithApps(
                AttestationPreCheckDao.GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_FLOW_COUNT_CHECK,
                appRef);

        List<String> result = aipcSvc.calcLogicalFlowPreCheckFailures(appRef);

        assertTrue(result.isEmpty(), "should be allowed to attest as in the no-flows exemption group");
    }

    // --- New test cases for primary ratings logic ---

    @Test
    public void shouldFailIfPrimaryRatingsMandatoryAndNoneExist() {
        createMeasurableCategory(TEST_MEASURABLE_CATEGORY_ID, true); // Category allows primary ratings
        createSetting(ATTESTATION_PRECHECK_PRIMARY_FLAG_KEY, "[{\"measurableCategoryId\": " + TEST_MEASURABLE_CATEGORY_ID + ", \"isPrimaryMandatory\": true}]");
        List<String> failures = aipcSvc.calcViewpointPreCheckFailures(TEST_ENTITY_REF, TEST_MEASURABLE_CATEGORY_ID);

        assertFalse(failures.isEmpty(), "Should fail because primary ratings are mandatory but none exist");
        assertTrue(failures.get(0).contains("atleast one primary viewpoint should be present"), "Failure message should indicate missing primary ratings");
    }

    @Test
    public void shouldPassIfPrimaryRatingsNotMandatory() {
        createMeasurableCategory(TEST_MEASURABLE_CATEGORY_ID, true); // Category allows primary ratings
        createSetting(ATTESTATION_PRECHECK_PRIMARY_FLAG_KEY, "[{\"measurableCategoryId\": " + TEST_MEASURABLE_CATEGORY_ID + ", \"isPrimaryMandatory\": false}]");

        List<String> failures = aipcSvc.calcViewpointPreCheckFailures(TEST_ENTITY_REF, TEST_MEASURABLE_CATEGORY_ID);

        assertTrue(failures.isEmpty(), "Should pass because primary ratings are not mandatory, even if none exist");
    }

    @Test
    public void shouldPassIfPrimaryRatingsMandatoryAndExist() {
        createMeasurableCategory(TEST_MEASURABLE_CATEGORY_ID, true); // Category allows primary ratings
        createSetting(ATTESTATION_PRECHECK_PRIMARY_FLAG_KEY, "[{\"measurableCategoryId\": " + TEST_MEASURABLE_CATEGORY_ID + ", \"isPrimaryMandatory\": true}]");

        long measurableId = counter.incrementAndGet();
        createMeasurable(measurableId, TEST_MEASURABLE_CATEGORY_ID);
        createMeasurableRating(TEST_ENTITY_REF, measurableId, true); // Create a primary rating

        List<String> failures = aipcSvc.calcViewpointPreCheckFailures(TEST_ENTITY_REF, TEST_MEASURABLE_CATEGORY_ID);

        assertTrue(failures.isEmpty(), "Should pass because primary ratings are mandatory and exist");
    }

    @Test
    public void shouldPassIfPrimaryRatingsNotAllowedByCategory() {
        createMeasurableCategory(TEST_MEASURABLE_CATEGORY_ID, false); // Category does NOT allow primary ratings
        createSetting(ATTESTATION_PRECHECK_PRIMARY_FLAG_KEY, "[{\"measurableCategoryId\": " + TEST_MEASURABLE_CATEGORY_ID + ", \"isPrimaryMandatory\": true}]");

        List<String> failures = aipcSvc.calcViewpointPreCheckFailures(TEST_ENTITY_REF, TEST_MEASURABLE_CATEGORY_ID);

        assertTrue(failures.isEmpty(), "Should pass because category does not allow primary ratings, so the check is skipped");
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


   /* @AfterEach
    public void removeExemptionGroups() {
        dsl.deleteFrom(Tables.APPLICATION_GROUP)
                .where(APPLICATION_GROUP.EXTERNAL_ID.eq(AttestationPreCheckDao.GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_FLOW_COUNT_CHECK))
                .execute();
    }*/

    private void createMeasurableCategory(long id, boolean allowPrimaryRatings) {
        dsl.insertInto(MEASURABLE_CATEGORY)
                .set(MEASURABLE_CATEGORY.ID, id)
                .set(MEASURABLE_CATEGORY.NAME, "Test Measurable Category " + id)
                .set(MEASURABLE_CATEGORY.DESCRIPTION, "Description for " + id)
                .set(MEASURABLE_CATEGORY.RATING_SCHEME_ID, 1L) // Assuming a default rating scheme exists
                .set(MEASURABLE_CATEGORY.LAST_UPDATED_BY, "test")
                .set(MEASURABLE_CATEGORY.LAST_UPDATED_AT, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()))
                .set(MEASURABLE_CATEGORY.EDITABLE, true)
                .set(MEASURABLE_CATEGORY.ALLOW_PRIMARY_RATINGS, allowPrimaryRatings)
                .execute();
    }

    private void createSetting(String key, String value) {
        dsl.insertInto(SETTINGS)
                .set(SETTINGS.NAME, key)
                .set(SETTINGS.VALUE, value)
                .set(SETTINGS.DESCRIPTION, "test")
                .set(SETTINGS.RESTRICTED, Boolean.FALSE)
                .execute();
    }

    private void createMeasurable(long id, long categoryId) {
        dsl.insertInto(MEASURABLE)
                .set(MEASURABLE.ID, id)
                .set(MEASURABLE.NAME, "Test Measurable " + id)
                .set(MEASURABLE.DESCRIPTION, "Desc")
                .set(MEASURABLE.MEASURABLE_CATEGORY_ID, categoryId)
                .set(MEASURABLE.CONCRETE, true)
                .set(MEASURABLE.LAST_UPDATED_BY, "test")
                .set(MEASURABLE.LAST_UPDATED_AT, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()))
                .execute();
    }

    private void createMeasurableRating(EntityReference entityRef, long measurableId, boolean isPrimary) {
        dsl.insertInto(MEASURABLE_RATING)
                .set(MEASURABLE_RATING.ENTITY_ID, entityRef.id())
                .set(MEASURABLE_RATING.ENTITY_KIND, entityRef.kind().name())
                .set(MEASURABLE_RATING.MEASURABLE_ID, measurableId)
                .set(MEASURABLE_RATING.RATING, "G") // Assuming 'G' is a valid rating
                .set(MEASURABLE_RATING.DESCRIPTION, "Test Rating")
                .set(MEASURABLE_RATING.LAST_UPDATED_BY, "test")
                .set(MEASURABLE_RATING.LAST_UPDATED_AT, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()))
                .set(MEASURABLE_RATING.IS_PRIMARY, isPrimary)
                .execute();
    }


    @AfterEach
    public void cleanup() {
        dsl.deleteFrom(Tables.APPLICATION_GROUP)
                .where(APPLICATION_GROUP.EXTERNAL_ID.eq(AttestationPreCheckDao.GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_FLOW_COUNT_CHECK))
                .execute();
        dsl.deleteFrom(Tables.APPLICATION_GROUP_ENTRY).execute();
        dsl.deleteFrom(Tables.DATA_TYPE).execute();
        dsl.deleteFrom(Tables.LOGICAL_FLOW).execute();
        dsl.deleteFrom(Tables.LOGICAL_FLOW_DECORATOR).execute();
        dsl.deleteFrom(MEASURABLE_RATING).execute();
        dsl.deleteFrom(MEASURABLE).execute();
        dsl.deleteFrom(MEASURABLE_CATEGORY).execute();
        dsl.deleteFrom(SETTINGS).execute();
    }


}