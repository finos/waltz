/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.database_information.DatabaseInformationDao;
import com.khartec.waltz.model.database_information.DatabaseInformation;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;


public class DatabaseHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DatabaseInformationDao databaseDao = ctx.getBean(DatabaseInformationDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<DatabaseInformation> dbs = databaseDao.findByApplicationId(801L);
        System.out.println(dbs.size());

        SelectConditionStep<Record1<Long>> idSelector = dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(801L, 802L, 803L));

        Map<Long, List<DatabaseInformation>> moreDbs = databaseDao.findByAppSelector(idSelector);
        System.out.println(moreDbs.size());
        System.out.println(moreDbs.values().size());



    }

}
