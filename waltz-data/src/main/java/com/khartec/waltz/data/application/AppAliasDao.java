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

package com.khartec.waltz.data.application;

import com.khartec.waltz.schema.tables.records.ApplicationAliasRecord;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.ApplicationAlias.APPLICATION_ALIAS;


@Repository
public class AppAliasDao {


    private static final Logger LOG = LoggerFactory.getLogger(AppAliasDao.class);

    private final DSLContext dsl;


    @Autowired
    public AppAliasDao(DSLContext dsl) {
        this.dsl = dsl;
    }



    public List<String> findAliasesForApplication(long appId) {
        return dsl.select(APPLICATION_ALIAS.ALIAS)
                .from(APPLICATION_ALIAS)
                .where(APPLICATION_ALIAS.APPLICATION_ID.eq(appId))
                .orderBy(APPLICATION_ALIAS.ALIAS.asc())
                .fetch(APPLICATION_ALIAS.ALIAS);
    }



    public int[] updateAliases(long id, List<String> aliases) {

        LOG.info("Updating aliases for application " + id + ", aliases: "+ aliases);

        dsl.delete(APPLICATION_ALIAS)
                .where(APPLICATION_ALIAS.APPLICATION_ID.eq(id))
                .execute();

        List<ApplicationAliasRecord> records = aliases.stream()
                .map(t -> new ApplicationAliasRecord(id, t))
                .collect(Collectors.toList());

        return dsl.batchInsert(records)
                .execute();
    }
}
