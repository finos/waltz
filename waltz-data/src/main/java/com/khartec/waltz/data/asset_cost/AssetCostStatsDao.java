/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import com.khartec.waltz.model.cost.CostBand;
import com.khartec.waltz.model.cost.ImmutableCost;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.RangeBandUtilities.toPrettyString;
import static com.khartec.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.AssetCost.ASSET_COST;

@Repository
public class AssetCostStatsDao {

    private static final String COST_KIND_CUMULATIVE = "CUMULATIVE";


    private final DSLContext dsl;

    private List<CostBand> costBands = newArrayList(
            Tuple.tuple(0, 1_000),
            Tuple.tuple(1_001, 5_000),
            Tuple.tuple(5_001, 10_000),
            Tuple.tuple(10_001, 500_000),
            Tuple.tuple(500_001, 1_000_000),
            Tuple.tuple(1_000_001, 5_000_000),
            Tuple.tuple(5_000_001, 10_000_000),
            Tuple.tuple(10_000_001, 100_000_000))
            .stream()
            .map(band -> new CostBand(
                    BigDecimal.valueOf(band.v1),
                    BigDecimal.valueOf(band.v2)))
            .collect(Collectors.toList());

    private Field<BigDecimal> subTotalAlias = DSL.field("subTotalAlias", BigDecimal.class);
    private Field<BigDecimal> subTotalSum = DSL.sum(ASSET_COST.AMOUNT);


    private List<Field<Integer>> costBandFields = costBands
            .stream()
            .map(band -> {
                Condition inRange = subTotalAlias.between(band.getLow(), band.getHigh());
                return DSL.count(DSL.when(inRange, 1)).as(toPrettyString(band));
            })
            .collect(Collectors.toList());


    @Autowired
    public AssetCostStatsDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<Tally<CostBand>> calculateCostBandStatisticsByAppIdSelector(int year, Select<Record1<Long>> appIdSelector) {
        Condition relevantAssetCodesCondition = relevantAssetCodesCondition(appIdSelector);
        return calculateCostBandStatistics(
                relevantAssetCodesCondition,
                year);
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
                        .kind(COST_KIND_CUMULATIVE)
                        .year(year)
                        .build());
    }
    // -----


    private Condition relevantAssetCodesCondition(Select<Record1<Long>> appIdSelector) {
        SelectConditionStep<Record1<String>> relevantAssetCodes =
                DSL.selectDistinct(APPLICATION.ASSET_CODE)
                        .from(APPLICATION)
                        .where(APPLICATION.ID.in(appIdSelector))
                        .and(IS_ACTIVE);
        return ASSET_COST.ASSET_CODE.in(relevantAssetCodes);
    }


    private List<Tally<CostBand>> calculateCostBandStatistics(Condition condition, int year) {

        SelectHavingStep<Record1<BigDecimal>> subTotals = DSL
                .select(subTotalSum.as(subTotalAlias))
                .from(ASSET_COST)
                .where(condition)
                .and(ASSET_COST.YEAR.eq(year))
                .groupBy(ASSET_COST.ASSET_CODE);

        SelectJoinStep<Record> query = dsl
                .select(costBandFields)
                .from(subTotals);

        return query
                .fetchOne(r ->
                        IntStream.range(0, costBandFields.size())
                                .mapToObj(fieldIdx -> {
                                    Integer count = r.getValue(fieldIdx, Integer.class);
                                    CostBand costBand = costBands.get(fieldIdx);
                                    return ImmutableTally.<CostBand>builder()
                                            .count(count)
                                            .id(costBand)
                                            .build();
                                })
                                .collect(Collectors.toList()));
    }

}
