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

package org.finos.waltz.service.licence;


import org.finos.waltz.data.licence.LicenceDao;
import org.finos.waltz.data.licence.LicenceIdSelectorFactory;
import org.finos.waltz.data.licence.search.LicenceSearchDao;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.licence.Licence;
import org.finos.waltz.model.licence.SaveLicenceCommand;
import org.finos.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class LicenceService {

    private final LicenceDao licenceDao;
    private final LicenceSearchDao licenceSearchDao;
    private final LicenceIdSelectorFactory licenceIdSelectorFactory = new LicenceIdSelectorFactory();


    @Autowired
    public LicenceService(LicenceDao licenceDao,
                          LicenceSearchDao licenceSearchDao) {
        checkNotNull(licenceDao, "licenceDao cannot be null");
        checkNotNull(licenceSearchDao, "licenceSearchDao cannot be null");
        this.licenceDao = licenceDao;
        this.licenceSearchDao = licenceSearchDao;
    }


    public List<Licence> findAll() {
        return licenceDao.findAll();
    }


    public Licence getById(long id) {
        return licenceDao.getById(id);
    }


    public Licence getByExternalId(String externalId) {
        return licenceDao.getByExternalId(externalId);
    }


    public List<Licence> findBySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = licenceIdSelectorFactory.apply(options);
        return licenceDao.findBySelector(selector);
    }


    public List<Tally<Long>> countApplications() {
        return licenceDao.countApplications();
    }

    public boolean save(SaveLicenceCommand cmd, String username) {
        return licenceDao.save(cmd, username);
    }

    public boolean remove(long licenceId, String username) {
        return licenceDao.remove(licenceId);
    }

    public Collection<Licence> search(EntitySearchOptions options) {
        return licenceSearchDao.search(options);
    }
}
