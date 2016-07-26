package com.khartec.waltz.service.software_catalog;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.software_catalog.SoftwarePackageDao;
import com.khartec.waltz.data.software_catalog.SoftwareUsageDao;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.software_catalog.*;
import com.khartec.waltz.model.tally.StringTally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.SoftwarePackage.SOFTWARE_PACKAGE;
import static com.khartec.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;

@Service
public class SoftwareCatalogService {

    private final SoftwarePackageDao softwarePackageDao;
    private final SoftwareUsageDao softwareUsageDao;
    private final ApplicationIdSelectorFactory factory;
    private final DSLContext dsl;


    @Autowired
    public SoftwareCatalogService(SoftwarePackageDao softwarePackageDao,
                                  SoftwareUsageDao softwareUsageDao,
                                  ApplicationIdSelectorFactory factory,
                                  DSLContext dsl) {

        Checks.checkNotNull(softwarePackageDao, "softwarePackageDao cannot be null");
        Checks.checkNotNull(softwareUsageDao, "softwareUsageDao cannot be null");
        Checks.checkNotNull(factory, "factory cannot be null");

        this.softwarePackageDao = softwarePackageDao;
        this.softwareUsageDao = softwareUsageDao;
        this.factory = factory;
        this.dsl = dsl;
    }


    public SoftwareCatalog findForAppIds(List<Long> appIds) {
        List<SoftwareUsage> usages = softwareUsageDao.findByAppIds(appIds);
        Set<Long> packageIds = usages.stream()
                .map(u -> u.softwarePackageId())
                .collect(Collectors.toSet());
        List<SoftwarePackage> packages =
                softwarePackageDao.findByIds(packageIds);

        return ImmutableSoftwareCatalog.builder()
                .usages(usages)
                .packages(packages)
                .build();
    }


    private List<StringTally> toTallies(Condition condition, Field groupingField) {
        return dsl.select(groupingField, DSL.count(groupingField))
                .from(SOFTWARE_PACKAGE)
                .innerJoin(SOFTWARE_USAGE)
                .on(SOFTWARE_PACKAGE.ID.eq(SOFTWARE_USAGE.SOFTWARE_PACKAGE_ID))
                .where(dsl.renderInlined(condition))
                .groupBy(groupingField)
                .fetch(JooqUtilities.TO_STRING_TALLY);
    }


    public SoftwareSummaryStatistics findStatisticsForAppIdSelector(ApplicationIdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = factory.apply(options);

        Condition condition = SOFTWARE_USAGE.APPLICATION_ID.in(appIdSelector);

        List<StringTally> vendorCounts = toTallies(condition, SOFTWARE_PACKAGE.VENDOR);
        List<StringTally> maturityCounts = toTallies(condition, SOFTWARE_PACKAGE.MATURITY_STATUS);

        return ImmutableSoftwareSummaryStatistics.builder()
                .vendorCounts(vendorCounts)
                .maturityCounts(maturityCounts)
                .build();
    }

}
