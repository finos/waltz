package com.khartec.waltz.jobs.sample.software_packages;

import com.khartec.waltz.jobs.sample.SampleDataUtilities;
import com.khartec.waltz.model.software_catalog.ImmutableSoftwarePackage;
import com.khartec.waltz.model.software_catalog.MaturityStatus;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;

public interface MiddlewareSoftwarePackages {

    SoftwarePackage mqSeries5 = ImmutableSoftwarePackage.builder()
            .vendor("IBM")
            .name("MQ Series")
            .version("5.0")
            .isNotable(true)
            .maturityStatus(MaturityStatus.DISINVEST)
            .provenance(SampleDataUtilities.SAMPLE_DATA_PROVENANCE)
            .description("MQSeries is an IBM software family whose components are used to tie together other software applications so that they can work together. This type of application is often known as business integration software or middleware.")
            .build();

    SoftwarePackage mqSeries6 = ImmutableSoftwarePackage.copyOf(mqSeries5)
            .withVersion("6")
            .withMaturityStatus(MaturityStatus.HOLD);

    SoftwarePackage mqSeries7 = ImmutableSoftwarePackage.copyOf(mqSeries5)
            .withVersion("7")
            .withMaturityStatus(MaturityStatus.INVEST);


    SoftwarePackage[] middleware = new SoftwarePackage[] {
            mqSeries5,
            mqSeries6,
            mqSeries7
    };

}
