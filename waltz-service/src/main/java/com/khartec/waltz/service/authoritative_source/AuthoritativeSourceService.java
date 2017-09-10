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

package com.khartec.waltz.service.authoritative_source;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSourceCreateCommand;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSourceUpdateCommand;
import com.khartec.waltz.model.authoritativesource.NonAuthoritativeSource;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.tables.AuthoritativeSource.AUTHORITATIVE_SOURCE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;


@Service
public class AuthoritativeSourceService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthoritativeSourceService.class);

    private final AuthoritativeSourceDao authoritativeSourceDao;
    private final AuthSourceRatingCalculator ratingCalculator;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private final OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory;
    private final ChangeLogService changeLogService;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;


    @Autowired
    public AuthoritativeSourceService(AuthoritativeSourceDao authoritativeSourceDao,
                                      AuthSourceRatingCalculator ratingCalculator,
                                      ApplicationIdSelectorFactory applicationIdSelectorFactory,
                                      DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                                      OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory,
                                      ChangeLogService changeLogService) {
        checkNotNull(authoritativeSourceDao, "authoritativeSourceDao must not be null");
        checkNotNull(ratingCalculator, "ratingCalculator cannot be null");
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");
        checkNotNull(organisationalUnitIdSelectorFactory, "organisationalUnitIdSelectorFactory cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.authoritativeSourceDao = authoritativeSourceDao;
        this.ratingCalculator = ratingCalculator;
        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.organisationalUnitIdSelectorFactory = organisationalUnitIdSelectorFactory;
        this.changeLogService = changeLogService;
    }


    public List<AuthoritativeSource> findByEntityKind(EntityKind kind) {
        return authoritativeSourceDao.findByEntityKind(kind);
    }


    public AuthoritativeSource getById(long id) {
        return authoritativeSourceDao.getById(id);
    }


    public List<AuthoritativeSource> findByEntityReference(EntityReference ref) {
        return authoritativeSourceDao.findByEntityReference(ref);
    }


    public List<AuthoritativeSource> findByApplicationId(long applicationId) {
        return authoritativeSourceDao.findByApplicationId(applicationId);
    }


    public int update(AuthoritativeSourceUpdateCommand command) {
        int updateCount = authoritativeSourceDao.update(command);
        AuthoritativeSource updatedAuthSource = getById(command.id().get());
        ratingCalculator.update(updatedAuthSource.dataType(), updatedAuthSource.parentReference());
        return updateCount;
    }


    public int insert(AuthoritativeSourceCreateCommand command) {
        int insertedCount = authoritativeSourceDao.insert(command);
        ratingCalculator.update(command.dataTypeId(), mkRef(EntityKind.ORG_UNIT, command.orgUnitId()));
        return insertedCount;
    }


    public int remove(long id) {
        AuthoritativeSource authSourceToDelete = getById(id);
        int deletedCount = authoritativeSourceDao.remove(id);
        ratingCalculator.update(authSourceToDelete.dataType(), authSourceToDelete.parentReference());
        return deletedCount;
    }


    public List<AuthoritativeSource> findAll() {
        return authoritativeSourceDao.findAll();
    }


    public boolean recalculateAllFlowRatings() {
        findAll()
                .forEach(authSource -> ratingCalculator.update(
                        authSource.dataType(),
                        authSource.parentReference()));

        return true;
    }


    public Map<EntityReference, Collection<EntityReference>> calculateConsumersForDataTypeIdSelector(
            IdSelectionOptions options) {
        Select<Record1<Long>> selector = dataTypeIdSelectorFactory.apply(options);
        return authoritativeSourceDao.calculateConsumersForDataTypeIdSelector(selector);
    }


    public Integer cleanupOrphans(String userId) {
        List<EntityReference> entityReferences = authoritativeSourceDao.cleanupOrphans();

        entityReferences
                .forEach(ref -> {
                    String message = ref.kind() == EntityKind.APPLICATION
                            ? "Removed as an authoritative source as declaring Org Unit no longer exists"
                            : "Application removed as an authoritative source as it no longer exists";

                    ChangeLog logEntry = ImmutableChangeLog.builder()
                            .parentReference(ref)
                            .message(message)
                            .severity(Severity.INFORMATION)
                            .operation(Operation.UPDATE)
                            .userId(userId)
                            .build();

                    changeLogService.write(logEntry);
                });

        return entityReferences.size();
    }


    public List<NonAuthoritativeSource> findNonAuthSources(EntityReference parentRef) {



        Condition customSelectionCriteria;
        switch(parentRef.kind()) {
            case DATA_TYPE:
                Select<Record1<Long>> dtSelector = dataTypeIdSelectorFactory.apply(mkOpts(
                        parentRef,
                        HierarchyQueryScope.CHILDREN));
                customSelectionCriteria = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(dtSelector);
                break;

            case ORG_UNIT:
                Select<Record1<Long>> ouSelector = organisationalUnitIdSelectorFactory.apply(mkOpts(
                        parentRef,
                        HierarchyQueryScope.CHILDREN));
                customSelectionCriteria = AuthoritativeSourceDao.CONSUMER_APP.ORGANISATIONAL_UNIT_ID.in(ouSelector);
                break;

            case MEASURABLE:
            case PERSON:
                customSelectionCriteria = mkConsumerSelectionCondition(parentRef, HierarchyQueryScope.CHILDREN);
                break;

            case APP_GROUP:
            case FLOW_DIAGRAM:
                customSelectionCriteria = mkConsumerSelectionCondition(parentRef, HierarchyQueryScope.EXACT);
                break;

            default:
                throw new UnsupportedOperationException("Cannot calculate non-auth sources for ref" + parentRef);
        }

        return authoritativeSourceDao.findNonAuthSources(customSelectionCriteria);
    }


    public List<AuthoritativeSource> findAuthSources(EntityReference parentRef) {
        Condition customSelectionCriteria;
        switch(parentRef.kind()) {
            case ORG_UNIT:
                Select<Record1<Long>> ouSelector = organisationalUnitIdSelectorFactory.apply(
                        mkOpts(parentRef, HierarchyQueryScope.CHILDREN));
                customSelectionCriteria = AUTHORITATIVE_SOURCE.PARENT_ID.in(ouSelector);
                break;
            case DATA_TYPE:
                Select<Record1<Long>> dtSelector = dataTypeIdSelectorFactory.apply(
                        mkOpts(parentRef, HierarchyQueryScope.CHILDREN));
                SelectConditionStep<Record1<String>> codeSelector = DSL
                        .select(DATA_TYPE.CODE)
                        .from(DATA_TYPE)
                        .where(DATA_TYPE.ID.in(dtSelector));
                customSelectionCriteria = AUTHORITATIVE_SOURCE.DATA_TYPE.in(codeSelector);
                break;
            case MEASURABLE:
            case PERSON:
                customSelectionCriteria = mkConsumerSelectionCondition(parentRef, HierarchyQueryScope.CHILDREN);
                break;
            case APP_GROUP:
            case FLOW_DIAGRAM:
                customSelectionCriteria = mkConsumerSelectionCondition(parentRef, HierarchyQueryScope.EXACT);
                break;
            default:
                throw new UnsupportedOperationException("Cannot calculate auth sources for ref" + parentRef);
        }

        return authoritativeSourceDao.findAuthSources(customSelectionCriteria);

    }


//    BiFunction<EntityReference, HierarchyQueryScope, Condition>
    private Condition mkConsumerSelectionCondition(EntityReference ref, HierarchyQueryScope scope) {
        Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(mkOpts(ref, scope));
        return AuthoritativeSourceDao.CONSUMER_APP.ID.in(appIdSelector);
    };
}
