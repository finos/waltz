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

package org.finos.waltz.service.flow_classification_rule;

import org.finos.waltz.data.flow_classification_rule.FlowClassificationDao;
import org.finos.waltz.model.flow_classification.FlowClassification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class FlowClassificationService {

    private final FlowClassificationDao flowClassificationDao;

    @Autowired
    public FlowClassificationService(FlowClassificationDao flowClassificationDao) {
        checkNotNull(flowClassificationDao, "flowClassificationDao must not be null");

        this.flowClassificationDao = flowClassificationDao;
    }

    public FlowClassification getById(long id) {
        return flowClassificationDao.getById(id);
    }

    public Set<FlowClassification> findAll() { return flowClassificationDao.findAll(); }

}
