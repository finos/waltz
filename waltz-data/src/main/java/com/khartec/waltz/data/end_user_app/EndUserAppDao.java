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

package com.khartec.waltz.data.end_user_app;

import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.enduserapp.ImmutableEndUserApplication;
import com.khartec.waltz.model.enduserapp.RiskRating;
import com.khartec.waltz.schema.tables.records.EndUserApplicationRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;

@Repository
public class EndUserAppDao {


    private final DSLContext dsl;

    private static final RecordMapper<Record, EndUserApplication> endUserAppMapper = r -> {
        EndUserApplicationRecord record = r.into(END_USER_APPLICATION);
        return ImmutableEndUserApplication.builder()
                .name(record.getName())
                .description(record.getDescription())
                .kind(record.getKind())
                .id(record.getId())
                .organisationalUnitId(record.getOrganisationalUnitId())
                .lifecyclePhase(LifecyclePhase.valueOf(record.getLifecyclePhase()))
                .riskRating(RiskRating.valueOf(record.getRiskRating()))
                .provenance(record.getProvenance())
                .build();
    };

    @Autowired
    public EndUserAppDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<EndUserApplication> findByOrganisationalUnitIds(List<Long> orgUnitIds) {
        return dsl.select(END_USER_APPLICATION.fields())
                .from(END_USER_APPLICATION)
                .where(END_USER_APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnitIds))
                .fetch(endUserAppMapper);

    }

}
