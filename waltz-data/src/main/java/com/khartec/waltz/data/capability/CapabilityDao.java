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

package com.khartec.waltz.data.capability;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.capability.Capability;
import com.khartec.waltz.model.capability.ImmutableCapability;
import com.khartec.waltz.schema.tables.AppCapability;
import com.khartec.waltz.schema.tables.records.CapabilityRecord;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.model.utils.IdUtilities.ensureHasId;
import static com.khartec.waltz.schema.tables.AppCapability.APP_CAPABILITY;
import static com.khartec.waltz.schema.tables.Capability.CAPABILITY;


@Repository
public class CapabilityDao {

    private static final Logger LOG = LoggerFactory.getLogger(CapabilityDao.class);


    public static final RecordMapper<Record, Capability> TO_DOMAIN_MAPPER = r -> {
        CapabilityRecord record = r.into(CapabilityRecord.class);
        return ImmutableCapability.builder()
                .id(record.getId())
                .level(record.getLevel())
                .level1(Optional.ofNullable(record.getLevel_1()))
                .level2(Optional.ofNullable(record.getLevel_2()))
                .level3(Optional.ofNullable(record.getLevel_3()))
                .level4(Optional.ofNullable(record.getLevel_4()))
                .level5(Optional.ofNullable(record.getLevel_5()))
                .parentId(Optional.ofNullable(record.getParentId()))
                .description(mkSafe(record.getDescription()))
                .name(record.getName())
                .build();
    };


    private final DSLContext dsl;
    private final com.khartec.waltz.schema.tables.Capability c = CAPABILITY.as("c");
    private final AppCapability ac = APP_CAPABILITY.as("ac");



    @Autowired
    public CapabilityDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<Capability> findAll() {
        return dsl
                .select(c.fields())
                .from(c)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public void assignLevels(Map<Long, Integer> levels) {
        for (Map.Entry<Long, Integer> entry : levels.entrySet()) {
            assignLevel(entry.getKey(), entry.getValue());
        }
    }


    private void assignLevel(long capabilityId, int level) {
        // NOTE: not using the alias because the generated MSSSQL is incorrect, #225
        dsl.update(CAPABILITY)
                .set(CAPABILITY.LEVEL, level)
                .where(CAPABILITY.ID.eq(capabilityId))
                .execute();
    }


    public List<Capability> findByIds(Long[] ids) {
        return dsl.select()
                .from(c)
                .where(c.ID.in(ids))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<Capability> findByAppIds(Long... appIds) {
         return dsl.select(c.fields())
                .from(c)
                .innerJoin(ac)
                .on(ac.CAPABILITY_ID.eq(c.ID))
                .where(ac.APPLICATION_ID.in(appIds))
                .fetch(TO_DOMAIN_MAPPER);
    }


    @Deprecated
    public boolean update(Capability capability) {
        ensureHasId(capability, "Cannot update capability record with no ID");

        LOG.info("Updating capability: "+capability);
        // NOTE: not using the alias because the generated MSSSQL is incorrect, #225
        return dsl.update(CAPABILITY)
                .set(CAPABILITY.NAME, capability.name())
                .set(CAPABILITY.DESCRIPTION, capability.description())
                .set(CAPABILITY.PARENT_ID, capability.parentId().orElse(null))
                .set(CAPABILITY.LEVEL, capability.level())
                .set(CAPABILITY.LEVEL_1, capability.level1().orElse(null))
                .set(CAPABILITY.LEVEL_2, capability.level2().orElse(null))
                .set(CAPABILITY.LEVEL_3, capability.level3().orElse(null))
                .set(CAPABILITY.LEVEL_4, capability.level4().orElse(null))
                .set(CAPABILITY.LEVEL_5, capability.level5().orElse(null))
                .where(CAPABILITY.ID.eq(capability.id().get()))
                .execute() == 1;
    }


    public Collection<Capability> findByIdSelector(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");
        return dsl.select(c.fields())
                .from(c)
                .where(c.ID.in(selector))
                .orderBy(c.NAME)
                .fetch(TO_DOMAIN_MAPPER);
    }
}
