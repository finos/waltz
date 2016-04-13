package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.software_catalog.SoftwarePackageDao;
import com.khartec.waltz.jobs.sample.software_packages.AppServerSoftwarePackages;
import com.khartec.waltz.jobs.sample.software_packages.DatabaseSoftwarePackages;
import com.khartec.waltz.jobs.sample.software_packages.MiddlewareSoftwarePackages;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.schema.tables.SoftwarePackage.SOFTWARE_PACKAGE;

public class SoftwarePackageGenerator {


    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        SoftwarePackageDao softwarePackageDao = ctx.getBean(SoftwarePackageDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<SoftwarePackage> softwarePackages = ListUtilities.builder(SoftwarePackage.class)
                .addAll(DatabaseSoftwarePackages.dbs)
                .addAll(AppServerSoftwarePackages.appServers)
                .addAll(MiddlewareSoftwarePackages.middleware)
                .build();


        System.out.println("-- deleting all software packages");
        dsl.deleteFrom(SOFTWARE_PACKAGE)
                .where(SOFTWARE_PACKAGE.PROVENANCE.eq(SampleDataUtilities.SAMPLE_DATA_PROVENANCE))
                .execute();

        System.out.println(" -- storing packages ( " + softwarePackages.size() + " )");
        softwarePackageDao.bulkStore(softwarePackages);

        System.out.println(" -- done");
    }



}
