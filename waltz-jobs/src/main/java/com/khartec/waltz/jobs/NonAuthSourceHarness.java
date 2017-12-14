/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

package com.khartec.waltz.jobs;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.authoritativesource.NonAuthoritativeSource;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.function.Consumer;

import static com.khartec.waltz.model.EntityReference.mkRef;


public class NonAuthSourceHarness {



    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        AuthoritativeSourceService authoritativeSourceService = ctx.getBean(AuthoritativeSourceService.class);

        Consumer<NonAuthoritativeSource> dumpRow = r -> {
            System.out.println(String.format("%s | %d - %d",
                    r.sourceReference().name().orElse("?"),
                    r.dataTypeId(),
                    r.count()));
        };

//        authoritativeSourceService.findNonAuthSources(mkRef(EntityKind.ORG_UNIT, 200L)).forEach(dumpRow);
//        authoritativeSourceService.findNonAuthSources(mkRef(EntityKind.DATA_TYPE, 6000L)).forEach(dumpRow);
        authoritativeSourceService.findNonAuthSources(mkRef(EntityKind.APP_GROUP, 41)).forEach(dumpRow);
    }


}
