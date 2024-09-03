package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.bulk_upload.ChangeOperation;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyValidatedItem;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyValidationResult;
import org.finos.waltz.model.bulk_upload.taxonomy.ChangedFieldType;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.taxonomy_management.BulkTaxonomyChangeService;
import org.finos.waltz.service.taxonomy_management.BulkTaxonomyItemParser;
import org.finos.waltz.test_common.helpers.MeasurableHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.common.CollectionUtilities.all;
import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BulkTaxonomyChangeServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private MeasurableService measurableService;

    @Autowired
    private BulkTaxonomyChangeService taxonomyChangeService;


    @Test
    public void previewBrandNewTaxonomy() {
        long categoryId = setupCategory();

        BulkTaxonomyValidationResult result = taxonomyChangeService.bulkPreview(
                categoryId,
                mkSimpleTsv(),
                BulkTaxonomyItemParser.InputFormat.CSV,
                BulkUpdateMode.ADD_ONLY);

        assertNotNull(result, "Expected a result");
        assertNoErrors(result);

        assertTrue(
                all(result.validatedItems(), d -> d.changeOperation() == ChangeOperation.ADD),
                "Should be all adds as we are using a new taxonomy");

        assertNoRemovals(result);
        assertExternalIdsMatch(result, asList("a1", "a1.1", "a1.2"));
    }


    @Test
    public void previewUpdates() {
        long categoryId = setupCategory();
        measurableHelper.createMeasurable("a1", "A1", categoryId);

        BulkTaxonomyValidationResult result = taxonomyChangeService.bulkPreview(
                categoryId,
                mkSimpleTsv(),
                BulkTaxonomyItemParser.InputFormat.CSV,
                BulkUpdateMode.ADD_ONLY);


        assertNotNull(result, "Expected a result");
        assertNoErrors(result);

        assertNoRemovals(result);
        assertExternalIdsMatch(result, asList("a1", "a1.1", "a1.2"));

        assertOperation(result, "a1", ChangeOperation.UPDATE);
        assertOperation(result, "a1.1", ChangeOperation.ADD);
        assertOperation(result, "a1.2", ChangeOperation.ADD);

        assertChangedFields(
                result,
                "a1",
                asSet(ChangedFieldType.DESCRIPTION, ChangedFieldType.CONCRETE));
    }


    @Test
    public void previewRemovalsIfModeIsReplace() {
        long categoryId = setupCategory();
        measurableHelper.createMeasurable("z1", "Z1", categoryId);

        BulkTaxonomyValidationResult result = taxonomyChangeService.bulkPreview(
                categoryId,
                mkSimpleTsv(),
                BulkTaxonomyItemParser.InputFormat.CSV,
                BulkUpdateMode.REPLACE);

        assertNotNull(result, "Expected a result");
        assertNoErrors(result);

        assertHasRemovals(result, asSet("z1"));
        assertExternalIdsMatch(result, asList("a1", "a1.1", "a1.2"));

        assertOperation(result, "a1", ChangeOperation.ADD);
        assertOperation(result, "a1.1", ChangeOperation.ADD);
        assertOperation(result, "a1.2", ChangeOperation.ADD);
    }


    @Test
    public void previewMultipleRemovalsIfModeIsReplace() {
        long categoryId = setupCategory();
        measurableHelper.createMeasurable("z1", "Z1", categoryId);
        measurableHelper.createMeasurable("z2", "Z2", categoryId);

        BulkTaxonomyValidationResult result = taxonomyChangeService.bulkPreview(
                categoryId,
                mkSimpleTsv(),
                BulkTaxonomyItemParser.InputFormat.CSV,
                BulkUpdateMode.REPLACE);

        assertNotNull(result, "Expected a result");
        assertNoErrors(result);

        assertHasRemovals(result, asSet("z1", "z2"));
        assertExternalIdsMatch(result, asList("a1", "a1.1", "a1.2"));

        assertOperation(result, "a1", ChangeOperation.ADD);
        assertOperation(result, "a1.1", ChangeOperation.ADD);
        assertOperation(result, "a1.2", ChangeOperation.ADD);
    }


    @Test
    public void applyBrandNewTaxonomy() {
        long categoryId = setupCategory();
        String user = setupUser();

        BulkTaxonomyValidationResult result = taxonomyChangeService.bulkPreview(
                categoryId,
                mkSimpleTsv(),
                BulkTaxonomyItemParser.InputFormat.CSV,
                BulkUpdateMode.ADD_ONLY);

        taxonomyChangeService.applyBulk(categoryId, result, user);

        List<Measurable> storedMeasurables = measurableService.findByCategoryId(categoryId);

        assertEquals(
            asSet("a1", "a1.1", "a1.2"),
            SetUtilities.map(storedMeasurables, m -> m.externalId().orElse(null)));
    }


    @Test
    public void applyUpdates() {
        long categoryId = setupCategory();
        String user = setupUser();
        measurableHelper.createMeasurable("a1", "A1", categoryId);

        BulkTaxonomyValidationResult result = taxonomyChangeService.bulkPreview(
                categoryId,
                mkSimpleTsv(),
                BulkTaxonomyItemParser.InputFormat.CSV,
                BulkUpdateMode.ADD_ONLY);

        taxonomyChangeService.applyBulk(categoryId, result, user);

        List<Measurable> storedMeasurables = measurableService.findByCategoryId(categoryId);

        assertEquals(
                asSet("a1", "a1.1", "a1.2"),
                SetUtilities.map(storedMeasurables, m -> m.externalId().orElse(null)));

        CollectionUtilities
                .find(storedMeasurables, m -> m.externalId().equals(Optional.of("a1")))
                .ifPresent(m -> {
                    assertEquals("Root node", m.description(), "Description should be updated");
                    assertFalse(m.concrete(), "Concrete flag should be unset");
                });
    }


    @Test
    public void applyRemovalsIfModeIsReplace() {
        long categoryId = setupCategory();
        String user = setupUser();
        measurableHelper.createMeasurable("z1", "Z1", categoryId);

        BulkTaxonomyValidationResult result = taxonomyChangeService.bulkPreview(
                categoryId,
                mkSimpleTsv(),
                BulkTaxonomyItemParser.InputFormat.CSV,
                BulkUpdateMode.REPLACE);

        taxonomyChangeService.applyBulk(categoryId, result, user);

        List<Measurable> storedMeasurables = measurableService.findByCategoryId(categoryId);
        assertEquals(
                asSet("a1", "a1.1", "a1.2"),
                SetUtilities.map(storedMeasurables, m -> m.externalId().orElse(null)),
                "z1 should be missing");
    }

    // --- HELPERS -----

    private long setupCategory() {
        String categoryName = mkName("TaxonomyChangeServiceTest", "category");
        long categoryId = measurableHelper.createMeasurableCategory(categoryName);
        return categoryId;
    }


    private String setupUser() {
        String username = mkName("TaxonomyChangeServiceTest", "user");
        userHelper.createUser(username);
        return username;
    }


    private void assertOperation(BulkTaxonomyValidationResult result,
                                 String externalId,
                                 ChangeOperation expectedOp) {
        Optional<BulkTaxonomyValidatedItem> item = maybeFindItem(result, externalId);

        assertTrue(item.isPresent(), "Expected to find item: " + externalId);
        assertEquals(item.get().changeOperation(), expectedOp, format("Expected item: %s to have op: %s",  externalId, expectedOp));
    }


    private void assertChangedFields(BulkTaxonomyValidationResult result,
                                     String externalId,
                                     Set<ChangedFieldType> expectedFields) {
        Optional<BulkTaxonomyValidatedItem> item = maybeFindItem(result, externalId);

        assertTrue(item.isPresent(), "Expected to find item: " + externalId);
        assertEquals(expectedFields, item.get().changedFields(), "Expected fields to have been changed");
    }


    private Optional<BulkTaxonomyValidatedItem> maybeFindItem(BulkTaxonomyValidationResult result,
                                                              String externalId) {
        Optional<BulkTaxonomyValidatedItem> item = find(
                result.validatedItems(),
                d -> d.parsedItem().externalId().equals(externalId));
        return item;
    }


    private void assertHasRemovals(BulkTaxonomyValidationResult result, Set<String> expectedRemovals) {
        assertEquals(expectedRemovals.size(), result.plannedRemovals().size(), "removals expected");
    }


    private void assertNoRemovals(BulkTaxonomyValidationResult result) {
        assertTrue(result.plannedRemovals().isEmpty(), "No removals expected");
    }

    private void assertExternalIdsMatch(BulkTaxonomyValidationResult result,
                                        List<String> expectedExternalIds) {
        assertEquals(
                expectedExternalIds,
                ListUtilities.map(result.validatedItems(), d -> d.parsedItem().externalId()),
                "Expected external ids do not match");
    }


    private void assertNoErrors(BulkTaxonomyValidationResult result) {
        assertTrue(
                all(result.validatedItems(), d -> isEmpty(d.errors())),
                "Should have no errors");
    }

    private String mkSimpleTsv() {
        return "externalId, parentExternalId, name, description, concrete\n" +
                "a1,, A1, Root node, false\n" +
                "a1.1, a1, A1_1, First child, true\n" +
                "a1.2, a1, A1_2, Second child, true\n";
    }

}
