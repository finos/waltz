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

import java.util.Random;


public class CapabilityGenerator {

    private static final Random rnd = new Random();


    public static void main(String[] args) {
//        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
//        CapabilityService capabilityDao = ctx.getBean(CapabilityService.class);
//        ApplicationService applicationDao = ctx.getBean(ApplicationService.class);
//        AppCapabilityService appCapabilityDao = ctx.getBean(AppCapabilityService.class);
//
//        DataSource dataSource = ctx.getBean(DataSource.class);
//        JdbcTemplate template = new JdbcTemplate(dataSource);
//        template.update("DELETE FROM app_capability");
//
//        List<Capability> capabilities = capabilityDao.findAll();
//
//        applicationDao.getAll().forEach(app -> {
//            int count = rnd.nextInt(3);
//
//            Set<Long> ids = IntStream
//                    .range(0, count)
//                    .mapToObj(i -> randomPick(capabilities))
//                    .map(c -> c.id().get())
//                    .collect(Collectors.toSet());
//
//            appCapabilityDao.addCapabilitiesToApp(app.id().get(), new ArrayList<>(ids));
//
//        });

    }
}
