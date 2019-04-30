/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.service.change_set;

import com.khartec.waltz.data.change_set.ChangeSetDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.change_set.ChangeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class ChangeSetService {

    private final ChangeSetDao changeSetDao;


    @Autowired
    public ChangeSetService(ChangeSetDao changeSetDao) {
        checkNotNull(changeSetDao, "changeSetDao cannot be null");
        this.changeSetDao = changeSetDao;
    }


    public ChangeSet getById(long id) {
        return changeSetDao.getById(id);
    }


    public List<ChangeSet> findByParentRef(EntityReference ref) {
        return changeSetDao.findByParentRef(ref);
    }
}
