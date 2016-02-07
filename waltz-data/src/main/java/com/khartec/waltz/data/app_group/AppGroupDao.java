package com.khartec.waltz.data.app_group;

import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.app_group.AppGroupKind;
import com.khartec.waltz.model.app_group.ImmutableAppGroup;
import com.khartec.waltz.schema.tables.ApplicationGroupMember;
import com.khartec.waltz.schema.tables.records.ApplicationGroupRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static com.khartec.waltz.schema.tables.ApplicationGroupMember.APPLICATION_GROUP_MEMBER;

@Repository
public class AppGroupDao {


    private static final RecordMapper<Record, AppGroup> groupMapper = r -> {
        ApplicationGroupRecord record = r.into(APPLICATION_GROUP);
        return ImmutableAppGroup.builder()
                .name(record.getName())
                .description(record.getDescription())
                .id(record.getId())
                .kind(AppGroupKind.valueOf(record.getKind()))
                .build();
    };


    private final DSLContext dsl;



    @Autowired
    public AppGroupDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public AppGroup getGroup(long groupId) {
        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.eq(groupId))
                .fetchOne(groupMapper);
    }


    public List<AppGroup> findGroupsForUser(String userId) {
        SelectConditionStep<Record1<Long>> groupIds = DSL.select(APPLICATION_GROUP_MEMBER.GROUP_ID)
                .from(APPLICATION_GROUP_MEMBER)
                .where(APPLICATION_GROUP_MEMBER.USER_ID.eq(userId));

        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.ID.in(groupIds))
                .fetch(groupMapper);
    }


    public List<AppGroup> findPublicGroups() {
        return dsl.select(APPLICATION_GROUP.fields())
                .from(APPLICATION_GROUP)
                .where(APPLICATION_GROUP.KIND.eq(AppGroupKind.PUBLIC.name()))
                .fetch(groupMapper);
    }
}
