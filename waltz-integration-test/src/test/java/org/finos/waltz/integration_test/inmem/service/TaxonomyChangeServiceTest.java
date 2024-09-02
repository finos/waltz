package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.bulk_upload.ChangeOperation;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyValidatedItem;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyValidationResult;
import org.finos.waltz.service.taxonomy_management.BulkTaxonomyItemParser;
import org.finos.waltz.service.taxonomy_management.TaxonomyChangeService;
import org.finos.waltz.test_common.helpers.MeasurableHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.finos.waltz.common.CollectionUtilities.all;
import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaxonomyChangeServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private TaxonomyChangeService taxonomyChangeService;


    @Test
    public void canHandleBrandNewTaxonomy() {
        String username = mkName("TaxonomyChangeServiceTest", "user");
        userHelper.createUser(username);

        String categoryName = mkName("TaxonomyChangeServiceTest", "category");
        long categoryId = measurableHelper.createMeasurableCategory(categoryName);

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
    public void identifiesUpdates() {
        String username = mkName("TaxonomyChangeServiceTest", "user");
        userHelper.createUser(username);

        String categoryName = mkName("TaxonomyChangeServiceTest", "category");
        long categoryId = measurableHelper.createMeasurableCategory(categoryName);
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
    }


    private void assertOperation(BulkTaxonomyValidationResult result,
                                 String externalId,
                                 ChangeOperation expectedOp) {
        Optional<BulkTaxonomyValidatedItem> item = find(
                result.validatedItems(),
                d -> d.parsedItem().externalId().equals(externalId));

        assertTrue(item.isPresent(), "Expected to find item: " + externalId);
        assertEquals(item.get().changeOperation(), expectedOp, format("Expected item: %s to have op: %s",  externalId, expectedOp));
    }


    // --- HELPERS -----


    private void assertNoRemovals(BulkTaxonomyValidationResult result) {
        assertEquals(0, result.plannedRemovalCount(), "No removals expected");
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
