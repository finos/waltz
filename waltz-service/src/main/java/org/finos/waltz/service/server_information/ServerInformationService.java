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

package org.finos.waltz.service.server_information;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.server_information.ServerInformationDao;
import org.finos.waltz.data.server_information.search.ServerInformationSearchDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.server_information.ServerInformation;
import org.finos.waltz.model.server_information.ServerSummaryBasicStatistics;
import org.finos.waltz.model.server_information.ServerSummaryStatistics;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.StringUtilities.isEmpty;


@Service
public class ServerInformationService {

    private final ApplicationIdSelectorFactory selectorFactory = new ApplicationIdSelectorFactory();
    private final ServerInformationDao serverInformationDao;
    private final ServerInformationSearchDao serverInformationSearchDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public ServerInformationService(ServerInformationDao serverInfoDao,
                                    ServerInformationSearchDao serverInformationSearchDao,
                                    ChangeLogService changeLogService) {
        checkNotNull(serverInfoDao, "serverInformationDao must not be null");
        checkNotNull(serverInformationSearchDao, "serverInformationSearchDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.serverInformationDao = serverInfoDao;
        this.serverInformationSearchDao = serverInformationSearchDao;
        this.changeLogService = changeLogService;
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

    /**
     * Upserts a batch of server assets (keyed on external id) and records a change-log entry
     * against each. Servers are part of the asset inventory; associating them with an
     * application is a separate concern handled via {@link org.finos.waltz.service.server_usage.ServerUsageService}.
     */
    public Collection<ServerInformation> bulkSave(List<ServerInformation> servers, String username) {
        checkNotNull(servers, "servers cannot be null");
        checkNotNull(username, "username cannot be null");
        if (servers.stream().anyMatch(s -> !s.externalId().isPresent())) {
            throw new IllegalArgumentException("every server must have an externalId (used as the upsert key)");
        }

        Collection<ServerInformation> saved = serverInformationDao.bulkUpsert(servers);

        Collection<ChangeLog> logs = saved
                .stream()
                .map(s -> ImmutableChangeLog.builder()
                        .message(format("Bulk saved server: %s", s.hostname()))
                        .parentReference(s.entityReference())
                        .userId(username)
                        .createdAt(DateTimeUtilities.nowUtc())
                        .severity(Severity.INFORMATION)
                        .childKind(EntityKind.SERVER)
                        .operation(Operation.UPDATE)
                        .build())
                .collect(Collectors.toList());
        changeLogService.write(logs);

        return saved;
    }


    public List<ServerInformation> search(String query) {
        if (isEmpty(query)) return emptyList();
        return search(EntitySearchOptions.mkForEntity(EntityKind.SERVER, query));
    }


    public List<ServerInformation> search(EntitySearchOptions options) {
        return serverInformationSearchDao.search(options);
    }


}
