package org.finos.waltz.integration_test.inmem.service;


import jdk.jfr.internal.Logger;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.data.measurable_category.MeasurableCategoryDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.bulk_upload.ChangeOperation;
import org.finos.waltz.model.bulk_upload.measurable_rating.BulkMeasurableRatingValidationResult;
import org.finos.waltz.model.bulk_upload.taxonomy.BulkTaxonomyValidationResult;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_rating.BulkMeasurableItemParser;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.MeasurableHelper;
import org.finos.waltz.test_common.helpers.RatingSchemeHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.finos.waltz.common.CollectionUtilities.all;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.*;

public class BulkMeasurableRatingServiceTest extends BaseInMemoryIntegrationTest {
    @Autowired
    private UserHelper userHelper;

    @Autowired
    private AppHelper appHelper;
    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private MeasurableCategoryDao measurableCategoryDao;

    @Autowired
    private RatingSchemeHelper ratingSchemeHelper;

    @Autowired
    private MeasurableService measurableService;

    @Autowired
    private MeasurableRatingService measurableRatingService;

    @Test
    public void previewUpdatesSuccess() {
        MeasurableCategory category = measurableCategoryDao.getById(setupCategory());
        measurableHelper.createMeasurable("CT-001", "M1", category.id().get());
        String app1AssetCode = mkName("assetCode1", "previewUpdates");
        EntityReference app1Ref = appHelper.createNewApp(
                mkName("app1", "previewUpdates"),
                ouIds.root,
                app1AssetCode);

        ratingSchemeHelper.saveRatingItem(category.ratingSchemeId(), "Rating1", 0, "#111", "R");
        BulkMeasurableRatingValidationResult result = measurableRatingService.bulkPreview(
                category.entityReference(),
                mkSimpleTsv(app1AssetCode),
                BulkMeasurableItemParser.InputFormat.CSV,
                BulkUpdateMode.ADD_ONLY);

        assertNotNull(result, "Expected a result");
        assertNoErrors(result);
        assertTaxonomyExternalIdMatch(result, asList("CT-001"));

    }

    @Test
    public void previewUpdatesError() {
        MeasurableCategory category = measurableCategoryDao.getById(setupCategory());
        measurableHelper.createMeasurable("CT-001", "M1", category.id().get());
        String app1AssetCode = mkName("assetCode1", "previewUpdates");
        EntityReference app1Ref = appHelper.createNewApp(
                mkName("app1", "previewUpdates"),
                ouIds.root,
                app1AssetCode);
        ratingSchemeHelper.saveRatingItem(category.ratingSchemeId(), "Rating1", 0, "#111", "R");
        BulkMeasurableRatingValidationResult result = measurableRatingService.bulkPreview(
                category.entityReference(),
                mkSimpleTsv(),
                BulkMeasurableItemParser.InputFormat.CSV,
                BulkUpdateMode.ADD_ONLY);

        assertNotNull(result, "Expected a result");
        assertNoErrors(result);
        assertTaxonomyExternalIdMatch(result, asList("CT-001"));
    }

    private long setupCategory() {
        String categoryName = mkName("MeasurableRatingChangeServiceTest", "category");
        return measurableHelper.createMeasurableCategory(categoryName);
    }

    private String mkSimpleTsv(String assetCode) {
        return "assetCode, taxonomyExternalId, ratingCode, isPrimary, comment\n"
                +assetCode+", CT-001, R, true, comment\n";
    }

    private void assertTaxonomyExternalIdMatch(BulkMeasurableRatingValidationResult result,
                                        List<String> taxonomyExternalId) {
        assertEquals(
                taxonomyExternalId,
                ListUtilities.map(result.validatedItems(), d -> d.parsedItem().taxonomyExternalId()),
                "Expected taxonomyExternalId do not match");
    }
    private void assertNoErrors(BulkMeasurableRatingValidationResult result) {
        assertTrue(
                all(result.validatedItems(), d -> isEmpty(d.errors())),
                "Should have no errors");
    }

    private String mkSimpleTsv() {
        return "assetCode, taxonomyExternalId, ratingCode, isPrimary, comment\n" +
                "assetCode1, CT-001, R, true, comment\n" +
                "assetCode2, CT-001, R, true, comment\n" ;
    }
}
