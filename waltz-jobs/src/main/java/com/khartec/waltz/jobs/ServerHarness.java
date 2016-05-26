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

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.data.server_info.ServerInfoDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import com.khartec.waltz.model.application.ImmutableApplicationIdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.server_info.ServerInfoService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutionException;


public class ServerHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ServerInfoService serverInfoService = ctx.getBean(ServerInfoService.class);
        ServerInfoDao serverInfoDao = ctx.getBean(ServerInfoDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);


        ApplicationIdSelectionOptions options = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.ORG_UNIT)
                        .id(10)
                        .build())
                .scope(HierarchyQueryScope.CHILDREN)
                .build();

        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("stats", () -> {
                try {
                    return serverInfoService.findStatsForAppSelector(options);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }


        String sql = "\n" +
                "select \n" +
                "  coalesce(\n" +
                "    sum(case [server_information].[is_virtual] when 1 then 1\n" +
                "                                               else 0\n" +
                "        end), \n" +
                "    0) [virtual_count], \n" +
                "  coalesce(\n" +
                "    sum(case [server_information].[is_virtual] when 1 then 0\n" +
                "                                               else 1\n" +
                "        end), \n" +
                "    0) [physical_count]\n" +
                "from [server_information]\n" +
                "where [server_information].[asset_code] in (\n" +
                "  select [application].[asset_code]\n" +
                "  from [application]\n" +
                "  where [application].[id] in (\n" +
                "    select [application].[id]\n" +
                "    from [application]\n" +
                "    where [application].[organisational_unit_id] in (\n" +
                "      130, 260, 70, 200, 10, 140, 270, 80, 210, 20, 150, 280, 90, 220, 30, 160, \n" +
                "      290, 100, 230, 40, 170, 300, 110, 240, 50, 180, 120, 250, 60, 190\n" +
                "    )\n" +
                "  )\n" +
                ");\n";



        FunctionUtilities.time(
                "raw q",
                () -> {
                    dsl.connection(conn -> {
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            System.out.println(rs.getBigDecimal(1) + " - " + rs.getBigDecimal(2));
                        }
                    });
                    return null;
                }
        );



    }




}
