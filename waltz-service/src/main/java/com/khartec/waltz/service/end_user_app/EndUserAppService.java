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

package com.khartec.waltz.service.end_user_app;

import com.khartec.waltz.data.end_user_app.EndUserAppDao;
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
    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory;


    @Autowired
    public EndUserAppService(EndUserAppDao endUserAppDao,
                             OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory) {
        checkNotNull(endUserAppDao, "EndUserAppDao is required");
        checkNotNull(orgUnitIdSelectorFactory, "orgUnitIdSelectorFactory cannot be null");

        this.endUserAppDao = endUserAppDao;
        this.orgUnitIdSelectorFactory = orgUnitIdSelectorFactory;
    }


    public List<EndUserApplication> findByOrganisationalUnitSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = orgUnitIdSelectorFactory.apply(options);
        return time("EUAS.findByOrganisationalUnitSelector", () -> endUserAppDao.findByOrganisationalUnitSelector(selector));
    }


    public Collection<Tally<Long>> countByOrgUnitId() {
        return time("EUAS.countByOrgUnitId", () -> endUserAppDao.countByOrganisationalUnit());
    }
}
