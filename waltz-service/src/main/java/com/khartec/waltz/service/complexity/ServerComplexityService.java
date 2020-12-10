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

package com.khartec.waltz.service.complexity;

import com.khartec.waltz.data.complexity.ServerComplexityDao;
import com.khartec.waltz.model.complexity.ComplexityKind;
import com.khartec.waltz.model.complexity.ComplexityScore;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.model.complexity.ComplexityUtilities.tallyToComplexityScore;

@Service
public class ServerComplexityService {

    private final ServerComplexityDao serverComplexityDao;


    @Autowired
    public ServerComplexityService(ServerComplexityDao serverComplexityDao) {
        this.serverComplexityDao = serverComplexityDao;
    }


    public List<ComplexityScore> findByAppIdSelector(Select<Record1<Long>> idSelector) {
        int baseline = serverComplexityDao.calculateBaseline();
        return findByAppIdSelector(idSelector, baseline);

    }


    public List<ComplexityScore> findByAppIdSelector(Select<Record1<Long>> idSelector, int baseline) {
        return serverComplexityDao.findCountsByAppIdSelector(idSelector)
                .stream()
                .map(tally -> tallyToComplexityScore(
                        ComplexityKind.SERVER,
                        tally,
                        baseline,
                        Math::log))
                .collect(Collectors.toList());
    }

}
