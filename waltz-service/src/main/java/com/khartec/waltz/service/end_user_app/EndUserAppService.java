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

package com.khartec.waltz.service.end_user_app;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.end_user_app.EndUserAppDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.tally.LongTally;
import com.khartec.waltz.model.utils.IdUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class EndUserAppService {

    private final EndUserAppDao endUserAppDao;
    private final OrganisationalUnitDao orgUnitDao;


    @Autowired
    public EndUserAppService(EndUserAppDao endUserAppDao, OrganisationalUnitDao orgUnitDao) {
        Checks.checkNotNull(endUserAppDao, "EndUserAppDao is required");
        Checks.checkNotNull(orgUnitDao, "OrgUnitDao is required");

        this.orgUnitDao = orgUnitDao;
        this.endUserAppDao = endUserAppDao;
    }


    public List<EndUserApplication> findByOrganisationalUnitIds(List<Long> ids) {
        return endUserAppDao.findByOrganisationalUnitIds(ids);
    }


    public List<EndUserApplication> findByOrganisationalUnitTree(long ouId) {
        List<Long> ids = IdUtilities.toIds(orgUnitDao.findDescendants(ouId));
        return findByOrganisationalUnitIds(ids);
    }


    public Collection<LongTally> countByOrgUnitId() {
        return endUserAppDao.countByOrganisationalUnit();
    }
}
