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

package com.khartec.waltz.service.attribute_change;

import com.khartec.waltz.data.attribute_change.AttributeChangeDao;
import com.khartec.waltz.model.attribute_change.AttributeChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class AttributeChangeService {

    private final AttributeChangeDao dao;


    @Autowired
    public AttributeChangeService(AttributeChangeDao dao) {
        checkNotNull(dao, "dao cannot be null");
        this.dao = dao;
    }


    public AttributeChange getById(long id) {
        return dao.getById(id);
    }


    public List<AttributeChange> findByChangeUnitId(long id) {
        return dao.findByChangeUnitId(id);
    }
}
