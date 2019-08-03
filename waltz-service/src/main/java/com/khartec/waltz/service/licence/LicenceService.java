/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019  Waltz open source project
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

package com.khartec.waltz.service.licence;


import com.khartec.waltz.data.licence.LicenceDao;
import com.khartec.waltz.data.licence.LicenceIdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.licence.Licence;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class LicenceService {

    private final LicenceDao licenceDao;
    private final LicenceIdSelectorFactory licenceIdSelectorFactory = new LicenceIdSelectorFactory();


    @Autowired
    public LicenceService(LicenceDao licenceDao) {
        checkNotNull(licenceDao, "licenceDao cannot be null");
        this.licenceDao = licenceDao;
    }


    public List<Licence> findAll() {
        return licenceDao.findAll();
    }


    public Licence getById(long id) {
        return licenceDao.getById(id);
    }


    public List<Licence> findBySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = licenceIdSelectorFactory.apply(options);
        return licenceDao.findBySelector(selector);
    }


    public List<Tally<Long>> countAppslications() {
        return licenceDao.countApplications();
    }
}
