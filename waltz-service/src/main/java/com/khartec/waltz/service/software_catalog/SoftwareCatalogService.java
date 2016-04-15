package com.khartec.waltz.service.software_catalog;

import com.khartec.waltz.data.software_catalog.SoftwarePackageDao;
import com.khartec.waltz.data.software_catalog.SoftwareUsageDao;
import com.khartec.waltz.model.software_catalog.ImmutableSoftwareCatalog;
import com.khartec.waltz.model.software_catalog.SoftwareCatalog;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;
import com.khartec.waltz.model.software_catalog.SoftwareUsage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SoftwareCatalogService {

    private final SoftwarePackageDao softwarePackageDao;
    private final SoftwareUsageDao softwareUsageDao;


    @Autowired
    public SoftwareCatalogService(SoftwarePackageDao softwarePackageDao, SoftwareUsageDao softwareUsageDao) {
        this.softwarePackageDao = softwarePackageDao;
        this.softwareUsageDao = softwareUsageDao;
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

}
