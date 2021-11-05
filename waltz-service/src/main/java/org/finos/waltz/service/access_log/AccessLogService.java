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

package org.finos.waltz.service.access_log;

import org.finos.waltz.data.access_log.AccessLogDao;
import org.finos.waltz.model.accesslog.AccessLog;
import org.finos.waltz.model.accesslog.AccessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;


@Service
public class AccessLogService {

    private final AccessLogDao accessLogDao;

    @Autowired
    public AccessLogService(AccessLogDao accessLogDao) {
        this.accessLogDao = accessLogDao;
    }


    public int write(AccessLog logEntry) {
        checkNotNull(logEntry, "logEntry must not be null");
        return accessLogDao.write(logEntry);
    }


    public List<AccessLog> findForUserId(String userId,
                                         Optional<Integer> limit) {
        checkNotEmpty(userId, "UserId must not be empty");
        return accessLogDao.findForUserId(userId, limit);
    }


    public List<AccessTime> findActiveUsersSince(Duration duration) {
        LocalDateTime sinceTime = nowUtc().minus(duration);
        return accessLogDao.findActiveUsersSince(sinceTime);
    }

}
