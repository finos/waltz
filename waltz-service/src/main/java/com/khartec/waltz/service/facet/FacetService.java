/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.facet;


import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;


@Service
public class FacetService {

    private final ApplicationDao applicationDao;

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public FacetService(ApplicationDao applicationDao) {
        checkNotNull(applicationDao, "applicationDao cannot be null");
        this.applicationDao = applicationDao;
    }


    public List<Tally<String>> getApplicationKindTallies(IdSelectionOptions options) {
        // we don't want the facets to apply and filter out non selected kinds, so we default to all kinds
        IdSelectionOptions appOptions = mkOpts(options.entityReference(), options.scope());
        Select<Record1<Long>> appSelector = applicationIdSelectorFactory.apply(appOptions);
        return applicationDao.countByApplicationKind(appSelector);
    }
}
