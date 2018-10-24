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

package com.khartec.waltz.service.server_information;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.server_information.ServerInformationDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.server_information.ServerInformation;
import com.khartec.waltz.model.server_information.ServerSummaryStatistics;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class ServerInformationService {

    private final ServerInformationDao serverInformationDao;
    private final ApplicationIdSelectorFactory selectorFactory;


    @Autowired
    public ServerInformationService(ServerInformationDao serverInfoDao, ApplicationIdSelectorFactory selectorFactory) {
        checkNotNull(serverInfoDao, "serverInformationDao must not be null");
        checkNotNull(selectorFactory, "selectorFactory cannot be null");

        this.serverInformationDao = serverInfoDao;
        this.selectorFactory = selectorFactory;
    }

    public List<ServerInformation> findByAssetCode(String assetCode) {
        return serverInformationDao.findByAssetCode(assetCode);
    }


    public List<ServerInformation> findByAppId(long appId) {
        return serverInformationDao.findByAppId(appId);
    }


    public ServerSummaryStatistics calculateStatsForAppSelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = selectorFactory.apply(options);
        return serverInformationDao.calculateStatsForAppSelector(selector);
    }

}
