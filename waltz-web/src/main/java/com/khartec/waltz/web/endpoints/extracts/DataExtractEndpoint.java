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

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;


@Service
public class DataExtractEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(DataExtractEndpoint.class);
    private static final String BASE_URL = mkPath("data-extract");

    private static AnnotationConfigApplicationContext ctx;

    @Autowired
    public DataExtractEndpoint(AnnotationConfigApplicationContext ctx) {
        checkNotNull(ctx, "ctx cannot be null");
        this.ctx = ctx;
    }


    @Override
    public void register() {
        Map<String, BaseDataExtractor> dataExtractors = ctx.getBeansOfType(BaseDataExtractor.class);
        dataExtractors.forEach((name, extractor) -> {
            LOG.info("Registering Data extractor: {}", name);
            extractor.register(BASE_URL);
        });
    }

}
