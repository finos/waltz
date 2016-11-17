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

import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.schema.tables.records.ApplicationTagRecord;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationTag.APPLICATION_TAG;


@Repository
public class AppTagDao {

    private static final Logger LOG = LoggerFactory.getLogger(AppTagDao.class);

    private final DSLContext dsl;


    @Autowired
    public AppTagDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<String> findAllTags() {
        return dsl.selectDistinct(APPLICATION_TAG.TAG)
                .from(APPLICATION_TAG)
                .orderBy(APPLICATION_TAG.TAG.asc())
                .fetch(APPLICATION_TAG.TAG);
    }


    public List<String> findTagsForApplication(long appId) {
        return dsl.select(APPLICATION_TAG.TAG)
                .from(APPLICATION_TAG)
                .where(APPLICATION_TAG.APPLICATION_ID.eq(appId))
                .orderBy(APPLICATION_TAG.TAG.asc())
                .fetch(APPLICATION_TAG.TAG);
    }


    public List<Application> findByTag(String tag) {
        if (isEmpty(tag)) { return Collections.emptyList(); }

        return dsl.select(APPLICATION.fields())
                .from(APPLICATION_TAG)
                .innerJoin(APPLICATION)
                .on(APPLICATION_TAG.APPLICATION_ID.eq(APPLICATION.ID))
                .where(APPLICATION_TAG.TAG.equalIgnoreCase(tag))
                .orderBy(APPLICATION.NAME)
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
    }


    public int[] updateTags(long id, Collection<String> tags) {
        LOG.info("Updating tags for application: {}, tags: {} ", id, tags);

        dsl.delete(APPLICATION_TAG)
                .where(APPLICATION_TAG.APPLICATION_ID.eq(id))
                .execute();

        List<ApplicationTagRecord> records = tags
                .stream()
                .map(t -> new ApplicationTagRecord(id, t))
                .collect(Collectors.toList());

        return dsl.batchInsert(records)
                .execute();
    }
}
