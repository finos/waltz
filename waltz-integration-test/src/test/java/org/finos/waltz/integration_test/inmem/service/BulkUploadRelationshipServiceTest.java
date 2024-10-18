package org.finos.waltz.integration_test.inmem.service;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.bulk_upload.entity_relationship.BulkUploadRelationshipApplyResult;
import org.finos.waltz.model.bulk_upload.entity_relationship.BulkUploadRelationshipValidationResult;
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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    ApplicationService applicationService;

    @Autowired
    MeasurableHelper measurableHelper;

    @Autowired
    MeasurableService measurableService;

    private static final Logger LOG = LoggerFactory.getLogger(BulkUploadRelationshipServiceTest.class);

    @Test
    public void testBulkUploadWithTargetCategory() {

        userHelper.createUserWithSystemRoles("test_user", SetUtilities.asSet(SystemRole.ADMIN));

        List<EntityReference> appList = new ArrayList<EntityReference>();

        EntityReference app1 = appHelper.createNewApp("tApp0", 65L, "t-0");
        EntityReference app2 = appHelper.createNewApp("tApp1", 63L, "t-1");
        EntityReference app3 = appHelper.createNewApp("tApp2", 66L, "t-2");
        EntityReference app4 = appHelper.createNewApp("tApp3", 64L, "t-3");

        List<Long> appIds = new ArrayList<>();
        appIds.add(app1.id());
        appIds.add(app2.id());
        appIds.add(app3.id());
        appIds.add(app4.id());

        List<Application> allApps = applicationService.findByIds(appIds);

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

        Collection<RelationshipKind> relationshipKinds = relationshipKindService.findAll();

        RelationshipKind relationshipKind = relationshipKinds
                .stream()
                .filter(t -> t.kindA() == EntityKind.APPLICATION
                        && t.kindB() == EntityKind.MEASURABLE
                        && t.categoryB() != null
                        && t.categoryB() == measurable1.categoryId())
                .collect(Collectors.toList())
                .get(0);

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

        EntityReference app5 = appHelper.createNewApp("tApp5", 65L, "t-4");
        EntityReference app6 = appHelper.createNewApp("tApp6", 63L, "t-5");
        EntityReference app7 = appHelper.createNewApp("tApp7", 66L, "t-6");
        EntityReference app8 = appHelper.createNewApp("tApp8", 64L, "t-7");

        appIds.clear();
        appIds.add(app5.id());
        appIds.add(app6.id());
        appIds.add(app7.id());
        appIds.add(app8.id());

        allApps = applicationService.findByIds(appIds);
        appList.clear();

        appList = allApps.stream().map(
                        t -> EntityReference.mkRef(t.kind(),
                                t.id().get(),
                                t.name(),
                                t.description(),
                                t.externalId().orElse("not")))
                .collect(Collectors.toList());

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

        BulkUploadRelationshipValidationResult previewResult = bulkUploadRelationshipService.bulkPreview(testInput, relationshipKind.id().get());
        BulkUploadRelationshipApplyResult applyResult = bulkUploadRelationshipService.bulkApply(previewResult, relationshipKind.id().get(), "test_user");

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

        long newMeasurableId = measurableHelper.createMeasurable("M_1", measurableHelper.createMeasurableCategory("testCat1"));

        EntityReference app9 = appHelper.createNewApp("tApp9", 65L, "t-9");
        EntityReference app10 = appHelper.createNewApp("tApp10", 63L, "t-10");
        EntityReference app11 = appHelper.createNewApp("tApp11", 66L, "t-11");
        EntityReference app12 = appHelper.createNewApp("tApp12", 64L, "t-12");

        List<Long> appIds = new ArrayList<>();
        appIds.add(app9.id());
        appIds.add(app10.id());
        appIds.add(app11.id());
        appIds.add(app12.id());


        List<EntityReference> appList;

        List<Application> allApps = applicationService.findByIds(appIds);
        Measurable measurable1 = measurableService.getById(newMeasurableId);

        appList = allApps.stream().map(
                        t -> EntityReference.mkRef(t.kind(),
                                t.id().get(),
                                t.name(),
                                t.description(),
                                t.externalId().orElse("not")))
                .collect(Collectors.toList());

        relationshipKindService.create(ImmutableRelationshipKind
                .builder()
                .kindB(EntityKind.APPLICATION)
                .kindA(EntityKind.MEASURABLE)
                .categoryA(measurable1.categoryId())
                .code("RELATES_TO_NEW")
                .name("Relates to new")
                .reverseName("Is related by new")
                .description("test rk new")
                .position(1)
                .build());

        Collection<RelationshipKind> relationshipKinds = relationshipKindService.findAll();

        RelationshipKind relationshipKind = relationshipKinds
                .stream()
                .filter(t -> t.kindA() == EntityKind.MEASURABLE
                        && t.kindB() == EntityKind.APPLICATION
                        && t.categoryA() != null
                        && t.categoryA() == measurable1.categoryId())
                .collect(Collectors.toList())
                .get(0);

        // inserting two relationships in the db to test against
        for(int i = 0; i < appList.size() - 2; i++) {
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

        appList = appList.subList(2, appList.size());

        final EntityReference measurable1ref = EntityReference.mkRef(
                EntityKind.MEASURABLE,
                measurable1.id().get(),
                measurable1.name(),
                measurable1.description(),
                measurable1.externalId().get());

        String testInput = mkGoodTestInput(measurable1ref, appList, "testDesc");
        testInput = testInput.concat(mkGoodTestRow("M_1", "t-9", "changed desc"));
        testInput = testInput.concat(mkGoodTestRow("M_1", "t-10", "changed desc"));
        testInput = testInput.concat(mkGoodTestRow("M_12", "t-0", "changed desc"));
        testInput = testInput.concat(mkGoodTestRow("M_1222", "t-0", "changed desc"));

        BulkUploadRelationshipValidationResult previewResult = bulkUploadRelationshipService.bulkPreview(testInput, relationshipKind.id().get());
        BulkUploadRelationshipApplyResult applyResult = bulkUploadRelationshipService.bulkApply(previewResult, relationshipKind.id().get(), "test_user");

        // expecting 4 relationships to be added
        assertEquals(2l, applyResult.recordsAdded().longValue());

        // expecting 2 records to be updated
        assertEquals(2l, applyResult.recordsUpdated().longValue());

        // expecting 2 rows to be skipped as these 2 are rows with errors
        assertEquals(2l, applyResult.skippedRows().longValue());
    }

    @Test
    public void testBulkUploadWithSourceAndTargetCategory() {

        userHelper.createUserWithSystemRoles("test_user", SetUtilities.asSet(SystemRole.ADMIN));

        long measurableId = measurableHelper.createMeasurable("M_2", measurableHelper.createMeasurableCategory("testCat2"));
        final Measurable measurable = measurableService.getById(measurableId);

        long measurableId1 = measurableHelper.createMeasurable("M_3", measurableHelper.createMeasurableCategory("testCat3"));
        final Measurable measurable1 = measurableService.getById(measurableId1);

        relationshipKindService.create(ImmutableRelationshipKind
                .builder()
                .name("Measures")
                .reverseName("Is measured by")
                .kindA(EntityKind.MEASURABLE)
                .kindB(EntityKind.MEASURABLE)
                .position(3)
                .code("MEASURED_BY")
                .categoryA(measurable.categoryId())
                .categoryB(measurable1.categoryId())
                .description("description")
                .build());

        List<RelationshipKind> relationshipKindList = relationshipKindService.findAll().stream().collect(Collectors.toList());
        RelationshipKind relationshipKind = null;

        for(int i = 0; i < relationshipKindList.size(); i++) {
            RelationshipKind rk = relationshipKindList.get(i);
            if(rk.kindA() == EntityKind.MEASURABLE
                && rk.kindB() == EntityKind.MEASURABLE
                && (rk.categoryA() != null && rk.categoryA().longValue() == measurable.categoryId())
                && (rk.categoryA() != null && rk.categoryB().longValue() == measurable1.categoryId())){
                relationshipKind = rk;
                break;
            }
        }

        String header = "source_external_id\ttarget_external_id\tdescription\n";
        String addInput = header + mkGoodTestRow(measurable.externalId().get(),
                measurable1.externalId().get(),
                "new relation");

        String updateInput = header + mkGoodTestRow(measurable.externalId().get(),
                measurable1.externalId().get(),
                "description");

        String noneInput = updateInput + "M_2\tM_9\tdesc\n";

        BulkUploadRelationshipValidationResult preview = bulkUploadRelationshipService.bulkPreview(addInput, relationshipKind.id().get());
        BulkUploadRelationshipApplyResult addApply = bulkUploadRelationshipService.bulkApply(preview, relationshipKind.id().get(), "test_user");

        preview = bulkUploadRelationshipService.bulkPreview(updateInput, relationshipKind.id().get());
        BulkUploadRelationshipApplyResult updateApply = bulkUploadRelationshipService.bulkApply(preview, relationshipKind.id().get(), "test_user");

        preview = bulkUploadRelationshipService.bulkPreview(noneInput, relationshipKind.id().get());
        BulkUploadRelationshipApplyResult noneApply = bulkUploadRelationshipService.bulkApply(preview, relationshipKind.id().get(), "test_user");

        assertEquals(1l, addApply.recordsAdded().longValue());
        assertEquals(1l, updateApply.recordsUpdated().longValue());
        assertEquals(2l, noneApply.skippedRows().longValue());
    }

    @Test
    public void testbulkUploadWithoutCategory() {

        userHelper.createUserWithSystemRoles("test_user", SetUtilities.asSet(SystemRole.ADMIN));

        EntityReference app9 = appHelper.createNewApp("tApp13", 65L, "t-13");
        EntityReference app10 = appHelper.createNewApp("tApp14", 63L, "t-14");
        EntityReference app11 = appHelper.createNewApp("tApp15", 66L, "t-15");
        EntityReference app12 = appHelper.createNewApp("tApp16", 64L, "t-16");

        List<Long> appIds = new ArrayList<>();
        appIds.add(app9.id());
        appIds.add(app10.id());
        appIds.add(app11.id());
        appIds.add(app12.id());

        List<Application> apps = applicationService.findByIds(appIds);

        List<EntityReference> apprefs = apps
                .stream()
                .map(t -> {
                    return EntityReference.mkRef(EntityKind.APPLICATION,
                            t.id().get(),
                            t.name(),
                            t.description(),
                            t.externalId().get());
                })
                .collect(Collectors.toList());

        relationshipKindService.create(ImmutableRelationshipKind.builder()
                .position(4)
                .kindA(EntityKind.APPLICATION)
                .kindB(EntityKind.APPLICATION)
                .name("Services")
                .reverseName("Serviced by")
                .code("SERVICE_TEST_2024")
                .description("description")
                .build());

        RelationshipKind relationshipKind = relationshipKindService.findAll()
                .stream()
                .filter(t -> StringUtilities.safeEq(t.code(), "SERVICE_TEST_2024"))
                .collect(Collectors.toList())
                .get(0);

        EntityReference app1 = apprefs.get(0);

        // create new relationships with 1 app
        for(int i = 1; i < apprefs.size() - 2; i++) {
            entityRelationshipService.createRelationship(ImmutableEntityRelationship
                    .builder()
                    .a(app1)
                    .b(apprefs.get(i))
                    .relationship(relationshipKind.code())
                    .lastUpdatedAt(LocalDateTime.now())
                    .lastUpdatedBy("test_user")
                    .description("testDesc")
                    .provenance("testProv")
                    .build()
            );
        }

        String addInput = mkGoodTestInput(app1, apprefs.subList(apprefs.size() - 2, apprefs.size()), "testDesc");
        String updateInput = mkGoodTestInput(app1, apprefs.subList(1, apprefs.size()), "changed desc");

        BulkUploadRelationshipValidationResult preview = bulkUploadRelationshipService.bulkPreview(addInput, relationshipKind.id().get());
        BulkUploadRelationshipApplyResult addApply = bulkUploadRelationshipService.bulkApply(preview, relationshipKind.id().get(), "test_user");

        preview = bulkUploadRelationshipService.bulkPreview(updateInput, relationshipKind.id().get());
        BulkUploadRelationshipApplyResult updateApply = bulkUploadRelationshipService.bulkApply(preview, relationshipKind.id().get(), "test_user");

        preview = bulkUploadRelationshipService.bulkPreview(updateInput, relationshipKind.id().get());
        BulkUploadRelationshipApplyResult noneApply = bulkUploadRelationshipService.bulkApply(preview, relationshipKind.id().get(), "test_user");

        assertEquals(2l, addApply.recordsAdded().longValue());
        assertEquals(3l, updateApply.recordsUpdated().longValue());
        assertEquals(3l, noneApply.skippedRows().longValue());
    }

    @Test
    public void testUnauthorized() {

        userHelper.createUserWithSystemRoles("test_user", SetUtilities.asSet(SystemRole.ADMIN));

        String header = "source_external_id\ttarget_external_id\tdescription\n";
        String testInput = header + mkGoodTestRow("asfsfee", "", "");
        testInput = testInput.concat(mkGoodTestRow("jde", "ere", "eerer"));

        BulkUploadRelationshipValidationResult previewResult = bulkUploadRelationshipService.bulkPreview(testInput, 13L);

        try {
            bulkUploadRelationshipService.bulkApply(previewResult, 13L, "error_test_user");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testErrorInput() {

        userHelper.createUserWithSystemRoles("test_user", SetUtilities.asSet(SystemRole.ADMIN));

        String testInput = mkGoodTestRow("asfsfee", "", "");
        testInput = testInput.concat(mkGoodTestRow("jde", "ere", "eerer"));

        BulkUploadRelationshipValidationResult previewResult = bulkUploadRelationshipService.bulkPreview(testInput, 13L);
        assertNotNull(previewResult.parseError());

        try {
            bulkUploadRelationshipService.bulkApply(previewResult, 13L, "test_user");
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
