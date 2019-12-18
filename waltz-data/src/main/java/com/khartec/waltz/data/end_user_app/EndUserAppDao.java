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

package com.khartec.waltz.data.end_user_app;

import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.enduserapp.ImmutableEndUserApplication;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.EndUserApplicationRecord;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static java.util.Optional.ofNullable;

@Repository
public class EndUserAppDao {


    private final DSLContext dsl;

    public static final RecordMapper<Record, EndUserApplication> TO_DOMAIN_MAPPER = r -> {
        EndUserApplicationRecord record = r.into(END_USER_APPLICATION);
        return ImmutableEndUserApplication.builder()
                .name(record.getName())
                .description(mkSafe(record.getDescription()))
                .externalId(ofNullable(record.getExternalId()))
                .applicationKind(record.getKind())
                .id(record.getId())
                .organisationalUnitId(record.getOrganisationalUnitId())
                .lifecyclePhase(LifecyclePhase.valueOf(record.getLifecyclePhase()))
                .riskRating(Criticality.valueOf(record.getRiskRating()))
                .provenance(record.getProvenance())
                .isPromoted(record.getIsPromoted())
                .build();
    };

    @Autowired
    public EndUserAppDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<Tally<Long>> countByOrganisationalUnit() {
        return JooqUtilities.calculateLongTallies(
                dsl,
                END_USER_APPLICATION,
                END_USER_APPLICATION.ORGANISATIONAL_UNIT_ID, DSL.trueCondition());
    }

    @Deprecated
    public List<EndUserApplication> findByOrganisationalUnitSelector(Select<Record1<Long>> selector) {
        return dsl.select(END_USER_APPLICATION.fields())
                .from(END_USER_APPLICATION)
                .where(END_USER_APPLICATION.ORGANISATIONAL_UNIT_ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<EndUserApplication> findBySelector(Select<Record1<Long>> selector) {
        return dsl.selectFrom(END_USER_APPLICATION)
                .where(END_USER_APPLICATION.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int updateIsPromotedFlag(long id) {
        return dsl.update(END_USER_APPLICATION)
                .set(END_USER_APPLICATION.IS_PROMOTED, true)
                .where(END_USER_APPLICATION.ID.eq(id))
                .execute();
    }

    public EndUserApplication getById(Long id) {
        return dsl
                .selectFrom(END_USER_APPLICATION)
                .where(END_USER_APPLICATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public List<EndUserApplication> findAll() {
        return dsl
                .selectFrom(END_USER_APPLICATION)
                .where(END_USER_APPLICATION.IS_PROMOTED.isFalse())
                .fetch(TO_DOMAIN_MAPPER);
    }
}
