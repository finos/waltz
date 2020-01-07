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

package com.khartec.waltz.service.user_agent_info;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.user_agent_info.UserAgentInfoDao;
import com.khartec.waltz.model.user_agent_info.UserAgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.*;

@Service
public class UserAgentInfoService {

    private final UserAgentInfoDao userAgentInfoDao;


    @Autowired
    public UserAgentInfoService(UserAgentInfoDao userAgentInfoDao) {
        checkNotNull(userAgentInfoDao, "userAgentInfoDao cannot be null");
        this.userAgentInfoDao = userAgentInfoDao;
    }


    public int save(UserAgentInfo userAgentInfo) {
        checkNotNull(userAgentInfo, "userLogin cannot be null");
        return userAgentInfoDao.save(userAgentInfo);
    }


    public List<UserAgentInfo> findLoginsForUser(String userName, int limit) {
        Checks.checkNotEmpty(userName, "username cannot be empty");
        checkTrue(limit > 0, "limit should be > 0");
        return userAgentInfoDao.findLoginsForUser(userName, limit);
    }

}
