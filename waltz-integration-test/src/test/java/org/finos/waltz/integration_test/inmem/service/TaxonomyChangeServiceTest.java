package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.service.taxonomy_management.BulkTaxonomyItemParser;
import org.finos.waltz.service.taxonomy_management.TaxonomyChangeService;
import org.finos.waltz.test_common.helpers.MeasurableHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Function;

import static org.finos.waltz.test_common.helpers.NameHelper.mkName;

public class TaxonomyChangeServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private TaxonomyChangeService taxonomyChangeService;


    @Test
    public void foo() {
        String username = mkName("TaxonomyChangeServiceTest", "user");
        userHelper.createUser(username);

        String categoryName = mkName("TaxonomyChangeServiceTest", "category");
        long categoryId = measurableHelper.createMeasurableCategory(categoryName);

        /*
          Validation checks:

          - unique ids
          - all parent id's exist either in file or in existing taxonomy
          - do a diff to determine
              - new
              - removed, if mode == replace
              - updated, match on external id

         */
        taxonomyChangeService.bulkPreview(
                categoryId,
                mkTsv(),
                BulkTaxonomyItemParser.InputFormat.TSV);


    }

    private String mkTsv() {
        return "externalId, parentExternalId, name, description, concrete\n" +
                "a1,, A1, Root node, false\n" +
                "a1.1, a1, A1_1, First child, true\n" +
                "a1.2, a1, A1_2, Second child, true\n";
    }

}
