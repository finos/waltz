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

import com.khartec.waltz.data.database_usage.DatabaseDao;
import com.khartec.waltz.model.database.Database;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.ListUtilities.newArrayList;


public class DatabaseHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DatabaseDao databaseDao = ctx.getBean(DatabaseDao.class);

        List<Database> dbs = databaseDao.findByApplicationId(801L);
        System.out.println(dbs.size());


        Map<Long, List<Database>> moreDbs = databaseDao.findByAppSelector(newArrayList(801L));
        System.out.println(moreDbs.size());
        System.out.println(moreDbs.values().size());



    }

}
