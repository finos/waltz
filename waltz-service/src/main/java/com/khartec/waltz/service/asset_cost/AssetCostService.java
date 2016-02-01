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
import com.khartec.waltz.model.cost.ApplicationCost;
import com.khartec.waltz.model.cost.AssetCost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AssetCostService {

    private final AssetCostDao assetCodeDao;


    @Autowired
    public AssetCostService(AssetCostDao assetCodeDao) {
        this.assetCodeDao = assetCodeDao;
    }


    public List<AssetCost> findByAssetCode(String code) {
        return assetCodeDao.findByAssetCode(code);
    }


    public List<AssetCost> findByAppId(long appId) {
        return assetCodeDao.findByAppId(appId);
    }

    public List<ApplicationCost> findAppCostsByAppIds(Long[] ids) {
        return assetCodeDao.findAppCostsByAppIds(ids);
    }
}
