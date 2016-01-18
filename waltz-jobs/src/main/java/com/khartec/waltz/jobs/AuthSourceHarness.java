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

import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceCalculator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;


public class AuthSourceHarness {




    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        AuthoritativeSourceCalculator authoritativeSourceCalculator = ctx.getBean(AuthoritativeSourceCalculator.class);

        long CTO_OFFICE = 40;
        long CTO_ADMIN = 400;
        long CEO_OFFICE = 10;
        long CIO_OFFICE = 20;
        long MARKET_RISK = 220;
        long CREDIT_RISK = 230;
        long OPERATIONS_IT = 200;
        long RISK = 210;

        long orgUnitId = CIO_OFFICE;

        authoritativeSourceCalculator.calculateAuthSourcesForOrgUnitTree(orgUnitId);

        long st = System.currentTimeMillis();
        for (int i = 0 ; i < 100; i ++ ) {
            authoritativeSourceCalculator.calculateAuthSourcesForOrgUnitTree(orgUnitId);
        }
        long end = System.currentTimeMillis();

        System.out.println("DUR " + (end - st));
        Map<Long, Map<String, Map<Long, AuthoritativeSource>>> rulesByOrgId = authoritativeSourceCalculator.calculateAuthSourcesForOrgUnitTree(orgUnitId);

        System.out.println("--CIO---");
        AuthoritativeSourceCalculator.prettyPrint(rulesByOrgId.get(CIO_OFFICE));

        System.out.println("--RISK---");
        AuthoritativeSourceCalculator.prettyPrint(rulesByOrgId.get(RISK));

        System.out.println("--MARKETRISK---");
        AuthoritativeSourceCalculator.prettyPrint(rulesByOrgId.get(MARKET_RISK));

        System.out.println("--OPS---");
        AuthoritativeSourceCalculator.prettyPrint(rulesByOrgId.get(OPERATIONS_IT));

        System.out.println("--CREDIT RISK---");
        AuthoritativeSourceCalculator.prettyPrint(rulesByOrgId.get(CREDIT_RISK));

    }



}
