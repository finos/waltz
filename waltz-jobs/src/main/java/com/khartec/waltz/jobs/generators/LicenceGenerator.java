/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
