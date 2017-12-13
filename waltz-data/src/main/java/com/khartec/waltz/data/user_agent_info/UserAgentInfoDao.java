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

package com.khartec.waltz.data.user_agent_info;

import com.khartec.waltz.model.user_agent_info.ImmutableUserAgentInfo;
import com.khartec.waltz.model.user_agent_info.UserAgentInfo;
import com.khartec.waltz.schema.tables.records.UserAgentInfoRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.UserAgentInfo.USER_AGENT_INFO;

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
    
    private final com.khartec.waltz.schema.tables.UserAgentInfo uai = USER_AGENT_INFO.as("uai");

    
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