package org.finos.waltz.integration_test.inmem.service;


import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.data.measurable_category.MeasurableCategoryDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.bulk_upload.measurable_rating.BulkMeasurableRatingValidatedItem;
import org.finos.waltz.model.bulk_upload.measurable_rating.BulkMeasurableRatingValidationResult;
import org.finos.waltz.model.bulk_upload.measurable_rating.ChangeOperation;
import org.finos.waltz.model.bulk_upload.measurable_rating.ValidationError;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_rating.BulkMeasurableItemParser;
import org.finos.waltz.service.measurable_rating.BulkMeasurableRatingService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.MeasurableHelper;
import org.finos.waltz.test_common.helpers.MeasurableRatingHelper;
import org.finos.waltz.test_common.helpers.RatingSchemeHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static org.finos.waltz.common.CollectionUtilities.all;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BulkMeasurableRatingServiceTest extends BaseInMemoryIntegrationTest {
    @Autowired
    private UserHelper userHelper;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private MeasurableRatingHelper measurableRatingHelper;

    @Autowired
    private MeasurableCategoryDao measurableCategoryDao;

    @Autowired
    private RatingSchemeHelper ratingSchemeHelper;

    @Autowired
    private MeasurableService measurableService;

    @Autowired
    private BulkMeasurableRatingService bulkMeasurableRatingService;

    private static final String stem = "BMRST";
    private static final Logger LOG = LoggerFactory.getLogger(BulkMeasurableRatingServiceTest.class);

    @Test
    public void previewAdds() {
        MeasurableCategory category = measurableCategoryDao.getById(setupCategory());
        measurableHelper.createMeasurable("CT-001", "M1", category.id().get());
        String app1AssetCode = mkName(stem, "previewUpdatesCode");
        EntityReference app1Ref = appHelper.createNewApp(
                mkName(stem, "previewUpdatesApp"),
                ouIds.root,
                app1AssetCode);

        ratingSchemeHelper.saveRatingItem(category.ratingSchemeId(), mkName(stem, "Rating1"), 0, "#111", "R", mkName(stem, "R"));
        BulkMeasurableRatingValidationResult result = bulkMeasurableRatingService.bulkPreview(
                category.entityReference(),
                mkGoodTsv(app1AssetCode),
                BulkMeasurableItemParser.InputFormat.TSV,
                BulkUpdateMode.ADD_ONLY);

        assertNotNull(result, "Expected a result");
        assertNoErrors(result);
        assertTaxonomyExternalIdMatch(result, asList("CT-001"));
    }


    @Test
    public void previewUpdates() {
        LOG.info("Setup app, measurable and rating scheme item");
        MeasurableCategory category = measurableCategoryDao.getById(setupCategory());
        long measurableId = measurableHelper.createMeasurable("CT-001", "M1", category.id().get());
        String app1AssetCode = mkName(stem, "previewUpdatesCode");
        EntityReference appRef = appHelper.createNewApp(
                mkName(stem, "previewUpdatesApp"),
                ouIds.root,
                app1AssetCode);
        ratingSchemeHelper.saveRatingItem(category.ratingSchemeId(), mkName(stem, "Rating1"), 0, "#111", "R", mkName(stem, "R"));

        LOG.info("Create an existing rating for the app");
        measurableRatingHelper.saveRatingItem(appRef, measurableId, "R", "user");

        BulkMeasurableRatingValidationResult result = bulkMeasurableRatingService.bulkPreview(
                category.entityReference(),
                mkGoodTsv(app1AssetCode),
                BulkMeasurableItemParser.InputFormat.TSV,
                BulkUpdateMode.ADD_ONLY);

        assertNotNull(result, "Expected a result");
        assertNoErrors(result);
        assertTaxonomyExternalIdMatch(result, asList("CT-001"));
        assertEquals(1, result.validatedItems().size(), "Only one parsed row expected");
        assertEquals(
                ChangeOperation.UPDATE,
                first(result.validatedItems()).changeOperation());
    }


    @Test
    public void previewComprehensive() {
        LOG.info("Setup app, measurable and rating scheme item");
        MeasurableCategory category = measurableCategoryDao.getById(setupCategory());
        long measurable1Id = measurableHelper.createMeasurable("CT-001", "M1", category.id().get());
        measurableHelper.createMeasurable("CT-002", "M2", category.id().get());

        String app1AssetCode = mkName(stem, "previewUpdatesCode");
        EntityReference appRef = appHelper.createNewApp(
                mkName(stem, "previewUpdatesApp"),
                ouIds.root,
                app1AssetCode);
        ratingSchemeHelper.saveRatingItem(category.ratingSchemeId(), mkName(stem, "Rating1"), 0, "#111", "R", mkName(stem, "R"));

        LOG.info("Create an existing rating for the app");
        measurableRatingHelper.saveRatingItem(appRef, measurable1Id, "R", "user");

        BulkMeasurableRatingValidationResult result = bulkMeasurableRatingService.bulkPreview(
                category.entityReference(),
                mkComprehensiveTsv(app1AssetCode, "CT-002", "CT-001"),
                BulkMeasurableItemParser.InputFormat.TSV,
                BulkUpdateMode.ADD_ONLY);

        assertNotNull(result, "Expected a result");
        assertEquals(3, result.validatedItems().size(), "Only one parsed row expected");
        assertEquals(
                asList(ChangeOperation.ADD, ChangeOperation.NONE, ChangeOperation.UPDATE),
                map(result.validatedItems(), BulkMeasurableRatingValidatedItem::changeOperation));
        assertEquals(
                asList(emptySet(), asSet(ValidationError.APPLICATION_NOT_FOUND), emptySet()),
                map(result.validatedItems(), BulkMeasurableRatingValidatedItem::errors));
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
        BulkMeasurableRatingValidationResult result = bulkMeasurableRatingService.bulkPreview(
                category.entityReference(),
                mkBadRefsTsv(app1AssetCode, "CT-001", 'R'),
                BulkMeasurableItemParser.InputFormat.TSV,
                BulkUpdateMode.ADD_ONLY);

        assertNotNull(result, "Expected a result");

        result
            .validatedItems()
            .forEach(d -> {
                if (d.parsedItem().assetCode().equals("badApp")) {
                    assertTrue(d.errors().contains(ValidationError.APPLICATION_NOT_FOUND), "Should be complaining about the bad app (assetCode)");
                }
                if (d.parsedItem().taxonomyExternalId().equals("badMeasurable")) {
                    assertTrue(d.errors().contains(ValidationError.MEASURABLE_NOT_FOUND), "Should be complaining about the bad measurable ref (taxonomyExtId)");
                }
                if (d.parsedItem().ratingCode() == '_') {
                    assertTrue(d.errors().contains(ValidationError.RATING_NOT_FOUND), "Should be complaining about the bad rating code");
                }
            });

        assertEquals(5, result.validatedItems().size(), "Expected 5 items");
        assertTrue(CollectionUtilities.all(result.validatedItems(), d -> ! d.errors().isEmpty()));
    }



    // --- helpers -------------------


    private long setupCategory() {
        String categoryName = mkName(stem, "category");
        return measurableHelper.createMeasurableCategory(categoryName);
    }


    private String mkGoodTsv(String assetCode) {
        return "assetCode\ttaxonomyExternalId\tratingCode\tisPrimary\tcomment\n"
                + assetCode + "\tCT-001\tR\ttrue\tcomment\n";
    }


    private void assertTaxonomyExternalIdMatch(BulkMeasurableRatingValidationResult result,
                                        List<String> taxonomyExternalId) {
        assertEquals(
                taxonomyExternalId,
                map(result.validatedItems(), d -> d.parsedItem().taxonomyExternalId()),
                "Expected taxonomyExternalId do not match");
    }


    private void assertNoErrors(BulkMeasurableRatingValidationResult result) {
        assertTrue(
                all(result.validatedItems(), d -> isEmpty(d.errors())),
                "Should have no errors");
    }


    private String mkBadRefsTsv(String goodApp,
                                String goodMeasurable,
                                char goodRating) {
        return "assetCode\ttaxonomyExternalId\tratingCode\tisPrimary\tcomment\n" +
                format("%s\t%s\t%s\ttrue\tcomment\n", goodApp, "badMeasurable", goodRating) +
                format("%s\t%s\t%s\ttrue\tcomment\n", "badApp", goodMeasurable, goodRating) +
                format("%s\t%s\t%s\ttrue\tcomment\n", goodApp, goodMeasurable, '_') +
                format("%s\t%s\t%s\ttrue\tcomment\n", goodApp, "badMeasurable", '_') +
                format("%s\t%s\t%s\ttrue\tcomment\n", "badApp", "badMeasurable", '_');
    }


    private String mkComprehensiveTsv(String goodApp,
                                      String goodMeasurable,
                                      String existingMeasurable) {
        return "assetCode\ttaxonomyExternalId\tratingCode\tisPrimary\tcomment\n" +
                format("%s\t%s\t%s\ttrue\tcomment\n", goodApp, goodMeasurable, 'R') +  // should be an 'add'
                format("%s\t%s\t%s\ttrue\tcomment\n", "badApp", goodMeasurable, 'R') +  // should be 'none' (with errors)
                format("%s\t%s\t%s\ttrue\tcomment\n", goodApp, existingMeasurable, 'R');  // should be 'update'
    }
}
