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

package com.khartec.waltz.service.end_user_app;

import com.khartec.waltz.data.end_user_app.EndUserAppDao;
import com.khartec.waltz.data.end_user_app.EndUserAppIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.FunctionUtilities.time;

@Service
public class EndUserAppService {

    private final EndUserAppDao endUserAppDao;
    private final EndUserAppIdSelectorFactory endUserAppIdSelectorFactory = new EndUserAppIdSelectorFactory();
    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory= new OrganisationalUnitIdSelectorFactory();


    @Autowired
    public EndUserAppService(EndUserAppDao endUserAppDao) {
        checkNotNull(endUserAppDao, "EndUserAppDao is required");
        this.endUserAppDao = endUserAppDao;
    }


    @Deprecated
    public List<EndUserApplication> findByOrganisationalUnitSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = orgUnitIdSelectorFactory.apply(options);
        return time("EUAS.findByOrganisationalUnitSelector", () -> endUserAppDao.findByOrganisationalUnitSelector(selector));
    }


    public Collection<Tally<Long>> countByOrgUnitId() {
        return time("EUAS.countByOrgUnitId", () -> endUserAppDao.countByOrganisationalUnit());
    }


    public List<EndUserApplication> findBySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = endUserAppIdSelectorFactory.apply(options);
        return time("EUAS.findBySelector", () -> endUserAppDao.findBySelector(selector));
    }

}
