package com.khartec.waltz.data.asset_cost;

import com.khartec.waltz.model.cost.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.RangeBandUtilities.toPrettyString;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.AssetCost.ASSET_COST;

@Repository
public class AssetCostStatsDao {

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


    public List<CostBandTally> calculateCostBandStatisticsByAppIds(AssetCostQueryOptions options) {
        Condition inScopeApps = byAppIdsCondition(options.applicationIds());
        int year = options.year();
        return calculateCostBandStatistics(
                inScopeApps,
                year);
    }


    public Cost calculateTotalCostByAppIds(AssetCostQueryOptions options) {
        Condition optionsCondition =
                APPLICATION.ID.in(options.applicationIds())
                        .and(ASSET_COST.YEAR.eq(options.year()));

        return dsl.select(DSL.coalesce(DSL.sum(ASSET_COST.AMOUNT), BigDecimal.ZERO))
                .from(ASSET_COST)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ASSET_CODE.eq(ASSET_COST.ASSET_CODE))
                .where(optionsCondition)
                .fetchOne(r -> ImmutableCost.builder()
                        .amount(r.value1())
                        .currencyCode("EUR")
                        .kind(CostKind.CUMLATIVE)
                        .year(options.year())
                        .build());
    }
    // -----


    private Condition byAppIdsCondition(Collection<Long> appIds) {
        SelectConditionStep<Record1<String>> relevantAssetCodes =
                DSL.selectDistinct(APPLICATION.ASSET_CODE)
                        .from(APPLICATION)
                        .where(APPLICATION.ID.in(appIds));
        return ASSET_COST.ASSET_CODE.in(relevantAssetCodes);
    }


    private List<CostBandTally> calculateCostBandStatistics(Condition condition, int year) {

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
                                    return ImmutableCostBandTally.builder()
                                            .count(count)
                                            .id(costBand)
                                            .build();
                                })
                                .collect(Collectors.toList()));
    }

}
