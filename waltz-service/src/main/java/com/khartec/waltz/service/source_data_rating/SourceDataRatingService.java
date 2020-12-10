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

package com.khartec.waltz.service.source_data_rating;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.source_data_rating.SourceDataRatingDao;
import com.khartec.waltz.model.source_data_rating.SourceDataRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class SourceDataRatingService {

    private final SourceDataRatingDao dao;

    @Autowired
    public SourceDataRatingService(SourceDataRatingDao dao) {
        Checks.checkNotNull(dao, "dao cannot be null");
        this.dao = dao;
    }

    public Collection<SourceDataRating> findAll() {
        return dao.findAll();
    }
}
