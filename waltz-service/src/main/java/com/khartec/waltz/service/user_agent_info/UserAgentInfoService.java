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
