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

package com.khartec.waltz.service.server_information;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.server_information.ServerInformationDao;
import com.khartec.waltz.data.server_information.search.ServerInformationSearchDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.server_information.ServerInformation;
import com.khartec.waltz.model.server_information.ServerSummaryBasicStatistics;
import com.khartec.waltz.model.server_information.ServerSummaryStatistics;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static java.util.Collections.emptyList;


@Service
public class ServerInformationService {

    private final ApplicationIdSelectorFactory selectorFactory = new ApplicationIdSelectorFactory();
    private final ServerInformationDao serverInformationDao;
    private final ServerInformationSearchDao serverInformationSearchDao;


    @Autowired
    public ServerInformationService(ServerInformationDao serverInfoDao,
                                    ServerInformationSearchDao serverInformationSearchDao) {
        checkNotNull(serverInfoDao, "serverInformationDao must not be null");
        checkNotNull(serverInformationSearchDao, "serverInformationSearchDao cannot be null");

        this.serverInformationDao = serverInfoDao;
        this.serverInformationSearchDao = serverInformationSearchDao;
    }

    public List<ServerInformation> findByAssetCode(String assetCode) {
        return serverInformationDao.findByAssetCode(assetCode);
    }


    public List<ServerInformation> findByAppId(long appId) {
        return serverInformationDao.findByAppId(appId);
    }


    public ServerInformation getById(long id) {
        return serverInformationDao.getById(id);
    }


    public ServerInformation getByExternalId(String externalId) {
        return serverInformationDao.getByExternalId(externalId);
    }


    public ServerInformation getByHostname(String hostname) {
        return serverInformationDao.getByHostname(hostname);
    }


    public ServerSummaryStatistics calculateStatsForAppSelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = selectorFactory.apply(options);
        return serverInformationDao.calculateStatsForAppSelector(selector);
    }

    public ServerSummaryBasicStatistics calculateBasicStatsForAppSelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = selectorFactory.apply(options);
        return serverInformationDao.calculateBasicStatsForAppSelector(selector);
    }

    public List<ServerInformation> search(String query) {
        if (isEmpty(query)) return emptyList();
        return search(EntitySearchOptions.mkForEntity(EntityKind.SERVER, query));
    }


    public List<ServerInformation> search(EntitySearchOptions options) {
        return serverInformationSearchDao.search(options);
    }


}
