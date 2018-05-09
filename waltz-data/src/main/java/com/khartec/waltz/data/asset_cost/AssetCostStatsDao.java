/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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
