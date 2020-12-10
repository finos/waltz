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

package com.khartec.waltz.service.change_set;

import com.khartec.waltz.data.change_set.ChangeSetDao;
import com.khartec.waltz.data.change_set.ChangeSetIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.change_set.ChangeSet;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class ChangeSetService {

    private final ChangeSetDao changeSetDao;
    private final ChangeSetIdSelectorFactory changeSetIdSelectorFactory = new ChangeSetIdSelectorFactory();


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


    public List<ChangeSet> findBySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = changeSetIdSelectorFactory.apply(options);
        return changeSetDao.findBySelector(selector);
    }


    public List<ChangeSet> findByPerson(String employeeId) {
        checkNotEmpty(employeeId, "employeeId cannot be null or empty");
        return changeSetDao.findByPerson(employeeId);
    }
}
