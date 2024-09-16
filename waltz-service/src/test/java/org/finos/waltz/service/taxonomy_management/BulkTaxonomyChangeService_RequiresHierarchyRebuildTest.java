package org.finos.waltz.service.taxonomy_management;

import org.finos.waltz.model.bulk_upload.ChangeOperation;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyValidatedItem;
import org.finos.waltz.model.bulk_upload.taxonomy.ChangedFieldType;
import org.finos.waltz.model.bulk_upload.taxonomy.ImmutableBulkTaxonomyItem;
import org.finos.waltz.model.bulk_upload.taxonomy.ImmutableBulkTaxonomyValidatedItem;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.service.taxonomy_management.BulkTaxonomyChangeService.requiresHierarchyRebuild;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BulkTaxonomyChangeService_RequiresHierarchyRebuildTest {

    private static final ImmutableBulkTaxonomyItem defaultItem = ImmutableBulkTaxonomyItem
            .builder()
            .name("foo")
            .externalId("baa")
            .build();

    @Test
    void doesNotRequireRebuildIfNoItems() {
        assertFalse(requiresHierarchyRebuild(emptySet()), "no items, therefore rebuild not required");
    }


    @Test
    void doesNotRequireRebuildIfItemsOnlyUpdateDecorations() {
        Set<BulkTaxonomyValidatedItem> itemsToCheck = asSet(
                ImmutableBulkTaxonomyValidatedItem
                        .builder()
                        .parsedItem(defaultItem)
                        .changeOperation(ChangeOperation.UPDATE)
                        .changedFields(asSet(ChangedFieldType.NAME))
                        .build());
        assertFalse(requiresHierarchyRebuild(itemsToCheck), "changes would not require a hierarchy rebuild");
    }


    @Test
    void requireRebuildIfItemsIncludeAdditions() {
        Set<BulkTaxonomyValidatedItem> itemsToCheck = asSet(
                ImmutableBulkTaxonomyValidatedItem
                        .builder()
                        .parsedItem(defaultItem)
                        .changeOperation(ChangeOperation.ADD)
                        .build());
        assertTrue(requiresHierarchyRebuild(itemsToCheck));
    }


    @Test
    void requireRebuildIfItemsChangeTheirParentID() {
        Set<BulkTaxonomyValidatedItem> itemsToCheck = asSet(
                ImmutableBulkTaxonomyValidatedItem
                        .builder()
                        .parsedItem(defaultItem)
                        .changeOperation(ChangeOperation.UPDATE)
                        .changedFields(asSet(ChangedFieldType.PARENT_EXTERNAL_ID))
                        .build());
        assertTrue(requiresHierarchyRebuild(itemsToCheck));
    }


    @Test
    void requireRebuildIfItemsIncludeRemovals() {
        Set<BulkTaxonomyValidatedItem> itemsToCheck = asSet(
                ImmutableBulkTaxonomyValidatedItem
                        .builder()
                        .parsedItem(defaultItem)
                        .changeOperation(ChangeOperation.REMOVE)
                        .build());
        assertTrue(requiresHierarchyRebuild(itemsToCheck));
    }


    @Test
    void requireRebuildIfItemsIncludeRestorations() {
        Set<BulkTaxonomyValidatedItem> itemsToCheck = asSet(
                ImmutableBulkTaxonomyValidatedItem
                        .builder()
                        .parsedItem(defaultItem)
                        .changeOperation(ChangeOperation.RESTORE)
                        .build());
        assertTrue(requiresHierarchyRebuild(itemsToCheck));
    }
}