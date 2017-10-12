/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.service.changelog;

import com.khartec.waltz.data.changelog.ChangeLogDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.changelog.ChangeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class ChangeLogService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeLogService.class);
    private final ChangeLogDao changeLogDao;


    @Autowired
    public ChangeLogService(ChangeLogDao changeLogDao) {
        checkNotNull(changeLogDao, "changeLogDao must not be null");

        this.changeLogDao = changeLogDao;
    }


    public List<ChangeLog> findByParentReference(EntityReference ref,
                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");

        return changeLogDao.findByParentReference(ref, limit);
    }


    public List<ChangeLog> findByUser(String userName,
                                      Optional<Integer> limit) {
        checkNotEmpty(userName, "Username cannot be empty");
        return changeLogDao.findByUser(userName, limit);
    }


    public int write(ChangeLog changeLog) {
        return changeLogDao.write(changeLog);
    }


    public int[] write(List<ChangeLog> changeLogs) {
        return changeLogDao.write(changeLogs);
    }

}
