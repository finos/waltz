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

package com.khartec.waltz.service.asset_cost;

import com.khartec.waltz.data.asset_cost.AssetCostDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.cost.ApplicationCost;
import com.khartec.waltz.model.cost.AssetCost;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.utils.IdUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AssetCostService {

    private final AssetCostDao assetCodeDao;
    private final OrganisationalUnitDao organisationalUnitDao;


    @Autowired
    public AssetCostService(AssetCostDao assetCodeDao, OrganisationalUnitDao organisationalUnitDao) {
        this.assetCodeDao = assetCodeDao;
        this.organisationalUnitDao = organisationalUnitDao;
    }


    public List<AssetCost> findByOrgUnitTree(long orgUnitId) {
        List<Long> orgUnitIds = getOrgUnitIds(orgUnitId);
        return assetCodeDao.findByOrgUnitIds(orgUnitIds);
    }


    public List<ApplicationCost> findAppCostsByOrgUnitTree(long orgUnitId) {
        List<Long> orgUnitIds = getOrgUnitIds(orgUnitId);
        return assetCodeDao.findAppCostsByOrgUnits(orgUnitIds);
    }


    public List<AssetCost> findByAssetCode(String code) {
        return assetCodeDao.findByAssetCode(code);
    }


    private List<Long> getOrgUnitIds(long orgUnitId) {
        List<OrganisationalUnit> orgUnits = organisationalUnitDao.findDescendants(orgUnitId);
        return IdUtilities.toIds(orgUnits);
    }

    public List<AssetCost> findByAppId(long appId) {
        return assetCodeDao.findByAppId(appId);
    }
}
