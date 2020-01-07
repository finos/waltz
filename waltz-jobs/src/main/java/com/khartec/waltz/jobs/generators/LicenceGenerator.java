/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.data.licence.LicenceDao;
import com.khartec.waltz.jobs.tools.importers.licence.finos.FinosLicenceComplianceImporter;
import com.khartec.waltz.jobs.tools.importers.licence.spdx.SpdxLicenceImporter;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static com.khartec.waltz.schema.Tables.LICENCE;

public class LicenceGenerator implements SampleDataGenerator {

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        try {
            DSLContext dsl = getDsl(ctx);
            LicenceDao licenceDao = ctx.getBean(LicenceDao.class);

            SpdxLicenceImporter spdxLicenceImporter = new SpdxLicenceImporter(dsl, licenceDao);
            FinosLicenceComplianceImporter finosImporter = new FinosLicenceComplianceImporter(dsl, licenceDao);

            spdxLicenceImporter.doImport();
            finosImporter.doImport();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(LICENCE)
                .execute();
        return true;
    }
}
