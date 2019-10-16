/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
