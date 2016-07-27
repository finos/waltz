/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.asset_cost;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.cost.*;
import com.khartec.waltz.schema.tables.records.AssetCostRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.AssetCost.ASSET_COST;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


@Repository
public class AssetCostDao {

    private final DSLContext dsl;

    private RecordMapper<Record, Cost> costMapper = r -> {
        AssetCostRecord record = r.into(ASSET_COST);
        return ImmutableCost.builder()
                .currencyCode(record.getCurrency())
                .amount(record.getAmount())
                .year(record.getYear())
                .kind(CostKind.valueOf(record.getKind()))
                .build();
    };

    private RecordMapper<Record, AssetCost> assetCostMapper = r -> {
        AssetCostRecord record = r.into(ASSET_COST);
        return ImmutableAssetCost.builder()
                .assetCode(record.getAssetCode())
                .cost(costMapper.map(r))
                .provenance(record.getProvenance())
                .build();
    };



    private RecordMapper<Record, ApplicationCost> appCostMapper = r -> {
        EntityReference appRef = ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(r.getValue(APPLICATION.ID))
                .name(r.getValue(APPLICATION.NAME))
                .build();

        return ImmutableApplicationCost.builder()
                .application(appRef)
                .cost(costMapper.map(r))
                .build();
    };


    @Autowired
    public AssetCostDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<AssetCost> findByAssetCode(String code) {
        return dsl.select(ASSET_COST.fields())
                .from(ASSET_COST)
                .where(ASSET_COST.ASSET_CODE.eq(code))
                .fetch(assetCostMapper);
    }


    public List<AssetCost> findByAppId(long appId) {
        return dsl.select(ASSET_COST.fields())
                .from(ASSET_COST)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ASSET_CODE.eq(ASSET_COST.ASSET_CODE))
                .where(APPLICATION.ID.eq(appId))
                .fetch(assetCostMapper);
    }


    public List<ApplicationCost> findAppCostsByAppIdSelector(int year, Select<Record1<Long>> appIdSelector) {
        List<SelectField<?>> fields = ListUtilities.push(
                Arrays.asList(ASSET_COST.fields()),
                APPLICATION.NAME,
                APPLICATION.ID,
                ORGANISATIONAL_UNIT.NAME,
                ORGANISATIONAL_UNIT.ID);

        return dsl.select(fields)
                .from(ASSET_COST)
                .innerJoin(APPLICATION)
                .on(ASSET_COST.ASSET_CODE.eq(APPLICATION.ASSET_CODE))
                .innerJoin(ORGANISATIONAL_UNIT)
                .on(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(ORGANISATIONAL_UNIT.ID))
                .where(APPLICATION.ID.in(appIdSelector))
                .and(ASSET_COST.YEAR.eq(year))
                .fetch(appCostMapper);
    }


    public Optional<Integer> findLatestYear() {
        Integer year = dsl
                .select(DSL.max(ASSET_COST.YEAR).as("latest"))
                .from(ASSET_COST)
                .fetchOne("latest", Integer.class);
        return Optional.ofNullable(year);
    }
}
