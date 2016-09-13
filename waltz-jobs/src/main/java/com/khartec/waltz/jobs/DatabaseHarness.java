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
import com.khartec.waltz.model.EndOfLifeStatus;
import com.khartec.waltz.model.database_information.DatabaseInformation;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.data.JooqUtilities.calculateStringTallies;
import static com.khartec.waltz.schema.tables.DatabaseInformation.DATABASE_INFORMATION;


public class DatabaseHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DatabaseInformationDao databaseDao = ctx.getBean(DatabaseInformationDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<DatabaseInformation> dbs = databaseDao.findByApplicationId(801L);
        System.out.println(dbs.size());


        List<Tally<String>> eolCounts = calculateStringTallies(
                dsl,
                DATABASE_INFORMATION,
                DSL.when(DATABASE_INFORMATION.END_OF_LIFE_DATE.lt(DSL.currentDate()), DSL.inline(EndOfLifeStatus.END_OF_LIFE.name()))
                        .otherwise(DSL.inline(EndOfLifeStatus.NOT_END_OF_LIFE.name())),
                DSL.trueCondition());

        System.out.println(eolCounts);



    }

}
