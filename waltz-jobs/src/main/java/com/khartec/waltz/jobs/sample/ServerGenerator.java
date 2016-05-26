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

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.server_info.ServerInfoDao;
import com.khartec.waltz.model.serverinfo.ImmutableServerInfo;
import com.khartec.waltz.model.serverinfo.ServerInfo;
import com.khartec.waltz.service.DIConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;


public class ServerGenerator {

    private static final Random rnd = new Random();


    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ServerInfoDao serverDao = ctx.getBean(ServerInfoDao.class);

        List<ServerInfo> servers = ListUtilities.newArrayList();

        IntStream.range(0, 100_000)
                .forEach(i -> servers.add(
                        ImmutableServerInfo.builder()
                                .hostname(mkHostName(i))
                                .environment(randomPick(SampleData.environments))
                                .location(randomPick(SampleData.locations))
                                .operatingSystem(randomPick(SampleData.operatingSystems))
                                .assetCode("wltz-0" + rnd.nextInt(4000))
                                .build()));

       // servers.forEach(System.out::println);
        serverDao.bulkSave(servers);


    }


    private static String mkHostName(int i) {
        return randomPick(SampleData.serverPrefixes)
                + i
                + randomPick(SampleData.serverPostfixes);
    }
}
