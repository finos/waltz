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

package com.khartec.waltz.model.entity_statistic;

/**
 * Created by dwatkins on 05/08/2016.
 */
public enum RollupKind {

    /**
     * This means group by outcome, then count distinct on entity references
     */
    COUNT_BY_ENTITY,

    /**
     * This means group by outcome, then sum the values in each group (entity references are discarded)
     */
    SUM_BY_VALUE,

    /**
     * This means group by outcome, then sum the values in each group (entity references are discarded)
     * then divide by the number of records in the group
     */
    AVG_BY_VALUE,

    /**
     * This means take the value as is from the database, no grouping or aggregation
     */
    NONE
}
