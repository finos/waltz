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