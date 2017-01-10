/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.complexity.CapabilityComplexityDao;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.complexity.CapabilityComplexityService;
import com.khartec.waltz.service.complexity.ComplexityRatingService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class CapabilityComplexityHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        CapabilityComplexityDao capabilityComplexityDao =
                ctx.getBean(CapabilityComplexityDao.class);

        CapabilityComplexityService capabilityComplexityService =
                ctx.getBean(CapabilityComplexityService.class);

        ComplexityRatingService complexityRatingService = ctx.getBean(ComplexityRatingService.class);

        sout(capabilityComplexityDao.findBaseline());

        System.out.println(capabilityComplexityService.getForApp(126L));

        System.out.println(complexityRatingService.getForApp(126L));
//        sout(capabilityComplexityDao.findBaseLineForOrgUnitIds(100L, 120L));
//        sout(capabilityComplexityDao.findScoresForOrgUnitIds(120L));
//        sout(capabilityComplexityService.findWithinOrgUnit(100L));
    }


    private static void measureDuration(Runnable r) {
        long st = System.currentTimeMillis();
        r.run();
        System.out.println("Duration: "+(System.currentTimeMillis() - st));
    }

    private static void sout(Object o) {
        System.out.println(o);
    }

}
