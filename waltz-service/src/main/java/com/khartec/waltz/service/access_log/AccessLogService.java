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

package com.khartec.waltz.service.access_log;

import com.khartec.waltz.data.access_log.AccessLogDao;
import com.khartec.waltz.model.accesslog.AccessLog;
import com.khartec.waltz.model.accesslog.AccessTime;
import org.jooq.tools.json.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;


@Service
public class AccessLogService {

    private final AccessLogDao accessLogDao;

    @Autowired
    public AccessLogService(AccessLogDao accessLogDao) {
        this.accessLogDao = accessLogDao;
    }


    public int write(AccessLog logEntry) throws ParseException {
        checkNotNull(logEntry, "logEntry must not be null");
        return accessLogDao.write(logEntry);
    }


    public List<AccessLog> findForUserId(String userId) {
        checkNotEmpty(userId, "UserId must not be empty");
        return accessLogDao.findForUserId(userId);
    }


    public List<AccessTime> findActiveUsersSince(Duration duration) {
        LocalDateTime sinceTime = nowUtc().minus(duration);
        return accessLogDao.findActiveUsersSince(sinceTime);
    }

}
