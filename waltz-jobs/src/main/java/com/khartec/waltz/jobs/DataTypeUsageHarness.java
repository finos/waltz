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

import com.khartec.waltz.data.data_type_usage.DataTypeUsageDao;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.AssetCodeRelationshipKind;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.ListUtilities.map;


public class DataTypeUsageHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DataTypeUsageService service = ctx.getBean(DataTypeUsageService.class);
        DataTypeUsageDao dao = ctx.getBean(DataTypeUsageDao.class);

//        EntityReference appReference = EntityReference.mkRef(EntityKind.APPLICATION, 1L, "Kiwi - 0");
//
//        service.recalculateForApplications(appReference);

        dao.recalculateForAllApplications();

    }


    private static void prettyPrint(Map<AssetCodeRelationshipKind, List<Application>> grouped) {
        grouped.forEach((key, apps) ->
                System.out.println(key.name() + map(apps, relatedApp -> "\n\t"+ toString(relatedApp))));
    }


    private static String toString(Application app) {
        return app.name() + " " + app.assetCode() + " / " + app.parentAssetCode();
    }

}
