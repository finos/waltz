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

package com.khartec.waltz.data.asset_cost;

import com.khartec.waltz.model.cost.Cost;
import com.khartec.waltz.model.cost.ImmutableCost;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.AssetCost.ASSET_COST;

@Repository
public class AssetCostStatsDao {

    private static final String COST_KIND_TOTAL = "TOTAL";


    private final DSLContext dsl;

    @Autowired
    public AssetCostStatsDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Cost calculateTotalCostByAppIdSelector(int year, Select<Record1<Long>> appIdSelector) {
        Condition optionsCondition =
                APPLICATION.ID.in(appIdSelector)
                        .and(ASSET_COST.YEAR.eq(year));

        return dsl.select(DSL.coalesce(DSL.sum(ASSET_COST.AMOUNT), BigDecimal.ZERO))
                .from(ASSET_COST)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ASSET_CODE.eq(ASSET_COST.ASSET_CODE))
                .where(optionsCondition)
                .fetchOne(r -> ImmutableCost.builder()
                        .amount(r.value1())
                        .costKind(COST_KIND_TOTAL)
                        .year(year)
                        .build());
    }
}
