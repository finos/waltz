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

package com.khartec.waltz.data.complexity;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;

@Repository
public class ServerComplexityDao {

    private static final Field<Integer> SERVER_COUNT_FIELD = DSL.field(DSL.count().as("server_count"));

    private final DSLContext dsl;


    @Autowired
    public ServerComplexityDao(DSLContext dsl) {
        this.dsl = dsl;
    }



    public List<Tally<String>> findCountsByAssetCodes(String... assetCodes) {
        return findCountsByAssetCodes(SERVER_INFORMATION.ASSET_CODE.in(assetCodes));
    }


    public int calculateBaseline() {
        return calculateBaseline(DSL.trueCondition());
    }


    public List<Tally<String>> findCounts() {
        return findCountsByAssetCodes(DSL.trueCondition());
    }


    public List<Tally<Long>> findCountsByAppIdSelector(Select<Record1<Long>> idSelector) {
        Checks.checkNotNull(idSelector, "idSelector cannot be null");

        return dsl.select(APPLICATION.ID, SERVER_COUNT_FIELD)
                .from(SERVER_INFORMATION)
                .innerJoin(APPLICATION)
                .on(SERVER_INFORMATION.ASSET_CODE.eq(APPLICATION.ASSET_CODE))
                .where(APPLICATION.ID.in(idSelector))
                .groupBy(APPLICATION.ID)
                .fetch(r -> ImmutableTally.<Long>builder()
                        .id(r.value1())
                        .count(r.value2())
                        .build());
    }


    private List<Tally<String>> findCountsByAssetCodes(Condition condition) {
        Checks.checkNotNull(condition, "Condition must be given, use DSL.trueCondition() for 'none'");

        return dsl.select(SERVER_INFORMATION.ASSET_CODE, SERVER_COUNT_FIELD)
                .from(SERVER_INFORMATION)
                .where(condition)
                .groupBy(SERVER_INFORMATION.ASSET_CODE)
                .orderBy(SERVER_COUNT_FIELD.desc())
                .fetch(r -> ImmutableTally.<String>builder()
                        .id(r.value1())
                        .count(r.value2())
                        .build());

    }


    private int calculateBaseline(Condition condition) {
        return dsl.select(DSL.max(SERVER_COUNT_FIELD))
                .from(DSL.select(SERVER_INFORMATION.ASSET_CODE, SERVER_COUNT_FIELD)
                        .from(SERVER_INFORMATION)
                        .where(condition)
                        .groupBy(SERVER_INFORMATION.ASSET_CODE))
                .fetchOne(r -> r.value1());
    }

}
