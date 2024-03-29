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

package org.finos.waltz.data.user_agent_info;

import org.finos.waltz.schema.tables.records.UserAgentInfoRecord;
import org.finos.waltz.model.user_agent_info.ImmutableUserAgentInfo;
import org.finos.waltz.model.user_agent_info.UserAgentInfo;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

import static org.finos.waltz.schema.tables.UserAgentInfo.USER_AGENT_INFO;
import static org.finos.waltz.common.Checks.checkNotNull;

@Repository
public class UserAgentInfoDao {

    private static final RecordMapper<? super Record, UserAgentInfo> TO_DOMAIN_MAPPPER = r -> {
        UserAgentInfoRecord record = r.into(USER_AGENT_INFO);
        return ImmutableUserAgentInfo.builder()
                .userName(record.getUserName())
                .resolution(record.getResolution())
                .userAgent(record.getUserAgent())
                .operatingSystem(record.getOperatingSystem())
                .ipAddress(record.getIpAddress())
                .loginTimestamp(record.getLoginTimestamp().toLocalDateTime())
                .build();
    };


    private static final Function<UserAgentInfo, UserAgentInfoRecord> TO_RECORD_MAPPER = domainObj -> {
        UserAgentInfoRecord record = new UserAgentInfoRecord();

        record.setUserName(domainObj.userName());
        record.setUserAgent(domainObj.userAgent());
        record.setResolution(domainObj.resolution());
        record.setOperatingSystem(domainObj.operatingSystem());
        record.setIpAddress(domainObj.ipAddress());
        record.setLoginTimestamp(Timestamp.valueOf(domainObj.loginTimestamp()));

        return record;
    };


    private final DSLContext dsl;
    
    private final org.finos.waltz.schema.tables.UserAgentInfo uai = USER_AGENT_INFO.as("uai");

    
    @Autowired
    public UserAgentInfoDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    
    public int save(UserAgentInfo userAgentInfo) {
        checkNotNull(userAgentInfo, "userLogin cannot be null");
        return dsl.executeInsert(TO_RECORD_MAPPER.apply(userAgentInfo));
    }


    public List<UserAgentInfo> findLoginsForUser(String userName, int limit) {
        return dsl.select(uai.fields())
                .from(uai)
                .where(uai.USER_NAME.eq(userName))
                .orderBy(uai.LOGIN_TIMESTAMP.desc())
                .limit(limit)
                .fetch(TO_DOMAIN_MAPPPER);
    }
}