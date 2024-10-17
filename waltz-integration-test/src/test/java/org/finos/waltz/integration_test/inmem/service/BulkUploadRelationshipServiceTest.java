package org.finos.waltz.integration_test.inmem.service;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.app_group.AppGroupDetail;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.bulk_upload.entity_relationship.BulkUploadRelationshipApplyResult;
import org.finos.waltz.model.bulk_upload.entity_relationship.BulkUploadRelationshipValidationResult;
import org.finos.waltz.model.entity_relationship.EntityRelationship;
import org.finos.waltz.model.entity_relationship.ImmutableEntityRelationship;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.rel.ImmutableRelationshipKind;
import org.finos.waltz.model.rel.RelationshipKind;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.app_group.AppGroupService;
import org.finos.waltz.service.application.ApplicationService;
import org.finos.waltz.service.entity_relationship.BulkUploadRelationshipService;
import org.finos.waltz.service.entity_relationship.EntityRelationshipService;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.relationship_kind.RelationshipKindService;
import org.finos.waltz.test_common.helpers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// do not run tests individually - run this test class instead
public class BulkUploadRelationshipServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    UserHelper userHelper;

    @Autowired
    RelationshipKindService relationshipKindService;

    @Autowired
    EntityRelationshipService entityRelationshipService;

    @Autowired
    BulkUploadRelationshipService bulkUploadRelationshipService;

    @Autowired
    AppHelper appHelper;

    @Autowired
    ActorHelper actorHelper;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    AppGroupHelper appGroupHelper;

    @Autowired
    AppGroupService appGroupService;

    @Autowired
    MeasurableHelper measurableHelper;

    @Autowired
    MeasurableService measurableService;

    private static final Logger LOG = LoggerFactory.getLogger(BulkUploadRelationshipServiceTest.class);

    @Test
    public void testBulkUploadWithTargetCategory() {

        userHelper.createUserWithSystemRoles("test_user", SetUtilities.asSet(SystemRole.ADMIN));

        List<EntityReference> appList = new ArrayList<EntityReference>();

        appHelper.createNewApp("tApp0", 65L, "t-0");
        appHelper.createNewApp("tApp1", 63L, "t-1");
        appHelper.createNewApp("tApp2", 66L, "t-2");
        appHelper.createNewApp("tApp3", 64L, "t-3");

        List<Application> allApps = applicationService.findAll();

        long cat1 = measurableHelper.createMeasurableCategory("testCat0");
        long msblId1 = measurableHelper.createMeasurable("M_0", "measurable0", cat1);
        Measurable measurable1 = measurableService.getById(msblId1);

        appList = allApps.stream().map(
                t -> EntityReference.mkRef(t.kind(),
                                t.id().get(),
                                t.name(),
                                t.description(),
                                t.externalId().orElse("not")))
                .collect(Collectors.toList());

        relationshipKindService.create(ImmutableRelationshipKind
                .builder()
                .kindA(EntityKind.APPLICATION)
                .kindB(EntityKind.MEASURABLE)
                .categoryB(measurable1.categoryId())
                .code("RELATES_TO")
                .name("Relates to")
                .reverseName("Is related by")
                .description("test rk")
                .position(1)
                .build());

        RelationshipKind relationshipKind = relationshipKindService.getById(28L);

        // inserting relationships in the db to test against
        for(int i = 0; i < appList.size(); i++) {
            EntityReference t = appList.get(i);
            entityRelationshipService.createRelationship(
                    ImmutableEntityRelationship
                    .builder()
                    .relationship(relationshipKind.code())
                    .a(t)
                    .b(EntityReference.mkRef(EntityKind.MEASURABLE,
                            msblId1, measurable1.name(), measurable1.description(),
                            measurable1.externalId().orElse("not")))
                    .lastUpdatedBy("test_user")
                    .lastUpdatedAt(LocalDateTime.now())
                    .provenance("testProv")
                    .description("testDesc")
                    .build());
        }

        appHelper.createNewApp("tApp5", 65L, "t-4");
        appHelper.createNewApp("tApp6", 63L, "t-5");
        appHelper.createNewApp("tApp7", 66L, "t-6");
        appHelper.createNewApp("tApp8", 64L, "t-7");

        allApps = applicationService.findAll();
        appList.clear();

        appList = allApps.stream().map(
                        t -> EntityReference.mkRef(t.kind(),
                                t.id().get(),
                                t.name(),
                                t.description(),
                                t.externalId().orElse("not")))
                .collect(Collectors.toList());

        appList = appList.subList(4, appList.size());

        final EntityReference measurable1ref = EntityReference.mkRef(
                EntityKind.MEASURABLE,
                measurable1.id().get(),
                measurable1.name(),
                measurable1.description(),
                measurable1.externalId().get());

        String testInput = mkGoodTestInput(appList, measurable1ref, "testDesc");
        testInput = testInput.concat(mkGoodTestRow("t-0", "M_0", "changed desc"));
        testInput = testInput.concat(mkGoodTestRow("t-1", "M_0", "changed desc"));
        testInput = testInput.concat(mkGoodTestRow("t-0222", "M_0", "changed desc"));
        testInput = testInput.concat(mkGoodTestRow("t-1222", "M_0", "changed desc"));

        BulkUploadRelationshipValidationResult previewResult = bulkUploadRelationshipService.bulkPreview(testInput, 28L);
        BulkUploadRelationshipApplyResult applyResult = bulkUploadRelationshipService.bulkApply(previewResult, 28L, "test_user");

        // expecting 4 relationships to be added
        assertEquals(4l, applyResult.recordsAdded().longValue());

        // expecting 2 records to be updated
        assertEquals(2l, applyResult.recordsUpdated().longValue());

        // expecting 2 rows to be skipped as these 2 are rows with errors
        assertEquals(2l, applyResult.skippedRows().longValue());
    }

    @Test
    public void testBulkUploadWithSourceCategory() {

        userHelper.createUserWithSystemRoles("test_user", SetUtilities.asSet(SystemRole.ADMIN));

        List<EntityReference> appList = new ArrayList<EntityReference>();

        List<Application> allApps = applicationService.findAll();

        Measurable measurable1 = measurableService.getById(1l);

        appList = allApps.stream().map(
                        t -> EntityReference.mkRef(t.kind(),
                                t.id().get(),
                                t.name(),
                                t.description(),
                                t.externalId().orElse("not")))
                .collect(Collectors.toList());

        appList = appList.subList(0, appList.size() - 4);

        relationshipKindService.create(ImmutableRelationshipKind
                .builder()
                .kindB(EntityKind.APPLICATION)
                .kindA(EntityKind.MEASURABLE)
                .categoryA(measurable1.categoryId())
                .code("RELATES_TO")
                .name("Relates to")
                .reverseName("Is related by")
                .description("test rk")
                .position(1)
                .build());

        RelationshipKind relationshipKind = relationshipKindService.getById(29L);

        // inserting relationships in the db to test against
        for(int i = 0; i < appList.size(); i++) {
            EntityReference t = appList.get(i);
            entityRelationshipService.createRelationship(
                    ImmutableEntityRelationship
                            .builder()
                            .relationship(relationshipKind.code())
                            .b(t)
                            .a(EntityReference.mkRef(EntityKind.MEASURABLE,
                                    measurable1.id().get(), measurable1.name(), measurable1.description(),
                                    measurable1.externalId().orElse("not")))
                            .lastUpdatedBy("test_user")
                            .lastUpdatedAt(LocalDateTime.now())
                            .provenance("testProv")
                            .description("testDesc")
                            .build());
        }

        allApps = applicationService.findAll();
        appList.clear();

        appList = allApps.stream().map(
                        t -> EntityReference.mkRef(t.kind(),
                                t.id().get(),
                                t.name(),
                                t.description(),
                                t.externalId().orElse("not")))
                .collect(Collectors.toList());

        appList = appList.subList(4, appList.size());

        final EntityReference measurable1ref = EntityReference.mkRef(
                EntityKind.MEASURABLE,
                measurable1.id().get(),
                measurable1.name(),
                measurable1.description(),
                measurable1.externalId().get());

        String testInput = mkGoodTestInput(measurable1ref, appList, "testDesc");
        testInput = testInput.concat(mkGoodTestRow("M_0", "t-0", "changed desc"));
        testInput = testInput.concat(mkGoodTestRow("M_0", "t-1", "changed desc"));
        testInput = testInput.concat(mkGoodTestRow("M_12", "t-0", "changed desc"));
        testInput = testInput.concat(mkGoodTestRow("M_1222", "t-0", "changed desc"));

        BulkUploadRelationshipValidationResult previewResult = bulkUploadRelationshipService.bulkPreview(testInput, 29L);
        BulkUploadRelationshipApplyResult applyResult = bulkUploadRelationshipService.bulkApply(previewResult, 29L, "test_user");

        // expecting 4 relationships to be added
        assertEquals(4l, applyResult.recordsAdded().longValue());

        // expecting 2 records to be updated
        assertEquals(2l, applyResult.recordsUpdated().longValue());

        // expecting 2 rows to be skipped as these 2 are rows with errors
        assertEquals(2l, applyResult.skippedRows().longValue());
    }

    @Test
    public void testErrorInput() {

        userHelper.createUserWithSystemRoles("test_user", SetUtilities.asSet(SystemRole.ADMIN));

        String testInput = mkGoodTestRow("asfsfee", "", "");
        testInput = testInput.concat(mkGoodTestRow("jde", "ere", "eerer"));

        BulkUploadRelationshipValidationResult previewResult = bulkUploadRelationshipService.bulkPreview(testInput, 13L);

        assertNotNull(previewResult.parseError());
        BulkUploadRelationshipApplyResult applyResult = null;
        try {
            applyResult = bulkUploadRelationshipService.bulkApply(previewResult, 13L, "test_user");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    // helpers
    private String mkGoodTestInput(List<EntityReference> srcItems, EntityReference targItem, String description) {
        return format("source_external_id\ttarget_external_id\tdescription\n" + mkGoodTestRows(srcItems, targItem, description));
    }

    private String mkGoodTestInput(EntityReference srcItem, List<EntityReference> targItems, String description) {
        return format("source_external_id\ttarget_external_id\tdescription\n" + mkGoodTestRows(srcItem, targItems, description));
    }

    private String mkGoodTestRow(String srcExtId, String tarExtId, String description) {
        return format("%s\t%s\t%s\n",
                srcExtId,
                tarExtId,
                description);
    }

    private String mkGoodTestRows(List<EntityReference> src, EntityReference tar, String description) {
        String result = "";
        for (int i = 0; i < src.size(); i++) {
            result += format("%s\t%s\t%s\n",
                    src.get(i).externalId().orElse("not"),
                    tar.externalId().orElse("not"),
                    description);
        }
        return result;
    }

    private String mkGoodTestRows(EntityReference src, List<EntityReference> tar, String description) {
        String result = "";
        for (int i = 0; i < tar.size(); i++) {
            result += format("%s\t%s\t%s\n",
                    src.externalId().orElse("not"),
                    tar.get(i).externalId().orElse("not"),
                    description);
        }
        return result;
    }


}
