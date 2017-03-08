/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.jobs;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.authoritativesource.AuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.schema.tables.records.DtaAuthRatingRecord;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceResolver;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.DataTypeAssociation.DATA_TYPE_ASSOCIATION;
import static com.khartec.waltz.schema.tables.DtaAuthRating.DTA_AUTH_RATING;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;

/**
 * Created by dwatkins on 06/03/2017.
 */
public class LogicalFlowTypeRatingHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        AuthoritativeSourceDao authoritativeSourceDao = ctx.getBean(AuthoritativeSourceDao.class);
        Select<Record1<Long>> appSelector = dsl
                .select(APPLICATION.ID)
                .from(APPLICATION);

        List<AuthoritativeRatingVantagePoint> vantagePoints = authoritativeSourceDao
                .findAuthoritativeRatingVantagePoints(SetUtilities.fromArray(50L));

        System.out.println(vantagePoints);

        HarnessUtilities.time("upd", () -> {
            dsl.transaction(cfg -> {
                DSLContext tx = DSL.using(cfg);
                ensureDefaultRatingsExist(tx);
                AuthoritativeSourceResolver resolver = createResolver(tx, appSelector, authoritativeSourceDao);
                List<DtaAuthRatingRecord> updates = determineUpdates(tx, appSelector, resolver);
                tx.batchUpdate(updates).execute();
            });
            return true;
        });

    }


    private static AuthoritativeSourceResolver createResolver(DSLContext dsl,
                                                              Select<Record1<Long>> appSelector,
                                                              AuthoritativeSourceDao authoritativeSourceDao) {
        Collection<Long> ouIds = dsl
                .selectDistinct(APPLICATION.ORGANISATIONAL_UNIT_ID)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(appSelector))
                .fetch(APPLICATION.ORGANISATIONAL_UNIT_ID);

        List<AuthoritativeRatingVantagePoint> vantagePoints = authoritativeSourceDao
                .findAuthoritativeRatingVantagePoints(SetUtilities.fromCollection(ouIds));

        return new AuthoritativeSourceResolver(vantagePoints);
    }


    private static List<DtaAuthRatingRecord> determineUpdates(DSLContext dsl,
                                                              Select<Record1<Long>> appSelector,
                                                              AuthoritativeSourceResolver resolver) {
        Condition appInScope = LOGICAL_FLOW.TARGET_ENTITY_ID.in(appSelector)
                .or(LOGICAL_FLOW.TARGET_ENTITY_ID.in(appSelector));

        Condition targetAppJoinCondition = APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        Condition logicalFlowJoinCondition = LOGICAL_FLOW.ID.eq(DATA_TYPE_ASSOCIATION.ENTITY_ID)
                .and(DATA_TYPE_ASSOCIATION.ENTITY_KIND.eq(EntityKind.LOGICAL_DATA_FLOW.name()));

        Condition dataTypeJoinCondition = DATA_TYPE.ID.eq(DATA_TYPE_ASSOCIATION.DATA_TYPE_ID);

        return dsl
                .select(
                        DATA_TYPE_ASSOCIATION.ID,
                        LOGICAL_FLOW.SOURCE_ENTITY_ID,
                        APPLICATION.ORGANISATIONAL_UNIT_ID,
                        DATA_TYPE.CODE)
                .from(DATA_TYPE_ASSOCIATION)
                .innerJoin(LOGICAL_FLOW)
                .on(logicalFlowJoinCondition)
                .innerJoin(APPLICATION)
                .on(targetAppJoinCondition)
                .innerJoin(DATA_TYPE)
                .on(dataTypeJoinCondition)
                .where(appInScope)
                .fetch()
                .stream()
                .map(r -> {
                    AuthoritativenessRating rating = resolver.resolve(
                            mkRef(EntityKind.ORG_UNIT, r.getValue(APPLICATION.ORGANISATIONAL_UNIT_ID)),
                            mkRef(EntityKind.APPLICATION, r.getValue(LOGICAL_FLOW.SOURCE_ENTITY_ID)),
                            r.getValue(DATA_TYPE.CODE));
                    Long associationId = r.getValue(DATA_TYPE_ASSOCIATION.ID);
                    DtaAuthRatingRecord updateRecord = dsl.newRecord(DTA_AUTH_RATING);
                    updateRecord.setAssociationId(associationId);
                    updateRecord.setRating(rating.name());
                    updateRecord.changed(DTA_AUTH_RATING.RATING);
                    return updateRecord;
                })
                .collect(Collectors.toList());
    }

    private static void ensureDefaultRatingsExist(DSLContext dsl) {
        //insert into dta_auth_rating (select id, 'NO_OPINION' from data_type_association where id not in (select association_id from dta_auth_rating));

        SelectConditionStep<Record2<Long, String>> toBeInserted = DSL.select(
                DATA_TYPE_ASSOCIATION.ID,
                DSL.value(AuthoritativenessRating.NO_OPINION.name()))
                .from(DATA_TYPE_ASSOCIATION)
                .where(DATA_TYPE_ASSOCIATION.ID.notIn(DSL.select(DTA_AUTH_RATING.ASSOCIATION_ID).from(DTA_AUTH_RATING)));

        dsl.insertInto(DTA_AUTH_RATING).select(toBeInserted).execute();
    }


}
