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

import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorRatingsService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by dwatkins on 03/09/2016.
 */
public class DataFlowDecoratorRatingsServiceHarness {

    public static void main(String[] args) throws SQLException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        LogicalFlowDecoratorRatingsService ratingService = ctx.getBean(LogicalFlowDecoratorRatingsService.class);
        AuthoritativeSourceService authoritativeSourceService = ctx.getBean(AuthoritativeSourceService.class);

        List<AuthoritativeSource> authSources = authoritativeSourceService.findAll();

        for (AuthoritativeSource authSource: authSources) {
            ratingService.updateRatingsForAuthSource(authSource.dataType(), authSource.parentReference());
        }

    }

}
