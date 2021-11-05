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

package com.khartec.waltz.service.measurable_category;

import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.measurable_category.MeasurableCategoryAlignmentViewDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.measurable.MeasurableCategoryAlignment;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MeasurableCategoryAlignmentViewService {

    private final MeasurableCategoryAlignmentViewDao measurableCategoryAlignmentViewDao;
    ApplicationIdSelectorFactory factory = new ApplicationIdSelectorFactory();


    @Autowired
    public MeasurableCategoryAlignmentViewService(MeasurableCategoryAlignmentViewDao measurableCategoryAlignmentViewDao) {
        this.measurableCategoryAlignmentViewDao = measurableCategoryAlignmentViewDao;
    }

    public Set<MeasurableCategoryAlignment> findAlignmentsByAppSelector(IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = factory.apply(options);
        return measurableCategoryAlignmentViewDao.findAlignmentsByAppSelector(appIdSelector);
    }

}
