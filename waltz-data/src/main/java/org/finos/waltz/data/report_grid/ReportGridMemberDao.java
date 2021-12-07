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

package org.finos.waltz.data.report_grid;


import org.finos.waltz.model.report_grid.ImmutableReportGridMember;
import org.finos.waltz.model.report_grid.ReportGridMember;
import org.finos.waltz.model.report_grid.ReportGridMemberRole;
import org.finos.waltz.schema.tables.records.ReportGridMemberRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.schema.Tables.REPORT_GRID_MEMBER;

@Repository
public class ReportGridMemberDao {

    private final DSLContext dsl;

    @Autowired
    public ReportGridMemberDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public static final RecordMapper<Record, ReportGridMember> TO_DOMAIN_MAPPER = r -> {
        ReportGridMemberRecord record = r.into(ReportGridMemberRecord.class);

        return ImmutableReportGridMember.builder()
                .groupId(record.getGridId())
                .userId(record.getUserId())
                .reportGridMemberRole(ReportGridMemberRole.valueOf(record.getRole()))
                .build();
    };

    public Set<ReportGridMember> findByGridId(Long gridId){
        return dsl
                .select(REPORT_GRID_MEMBER.fields())
                .from(REPORT_GRID_MEMBER)
                .where(REPORT_GRID_MEMBER.GRID_ID.eq(gridId))
                .fetchSet(TO_DOMAIN_MAPPER);
    }

    public int register(long gridId, String username, ReportGridMemberRole role) {
        return dsl
                .insertInto(REPORT_GRID_MEMBER)
                .set(REPORT_GRID_MEMBER.GRID_ID, gridId)
                .set(REPORT_GRID_MEMBER.USER_ID, username)
                .set(REPORT_GRID_MEMBER.ROLE, role.name())
                .execute();
    }

    public boolean canUpdate(long gridId, String userId) {
        return dsl
                .fetchExists(DSL
                .select(REPORT_GRID_MEMBER.GRID_ID)
                .from(REPORT_GRID_MEMBER)
                .where(REPORT_GRID_MEMBER.GRID_ID.eq(gridId)
                        .and(REPORT_GRID_MEMBER.USER_ID.eq(userId)
                                .and(REPORT_GRID_MEMBER.ROLE.eq(ReportGridMemberRole.OWNER.name())))));
    }
}

