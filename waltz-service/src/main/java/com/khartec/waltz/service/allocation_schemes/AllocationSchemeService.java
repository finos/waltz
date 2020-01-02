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

package com.khartec.waltz.service.allocation_schemes;

import com.khartec.waltz.data.allocation_scheme.AllocationSchemeDao;
import com.khartec.waltz.model.allocation_scheme.AllocationScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AllocationSchemeService {

    private final AllocationSchemeDao allocationSchemeDao;


    @Autowired
    public AllocationSchemeService(AllocationSchemeDao allocationSchemeDao) {
        this.allocationSchemeDao = allocationSchemeDao;
    }


    public List<AllocationScheme> findAll() {
        return allocationSchemeDao.findAll();
    }


    public List<AllocationScheme> findByCategoryId(long categoryId) {
        return allocationSchemeDao.findByCategoryId(categoryId);
    }


    public AllocationScheme getById(long id) {
        return allocationSchemeDao.getById(id);
    }


    public long create(AllocationScheme scheme) {
        return allocationSchemeDao.create(scheme);
    }

}
