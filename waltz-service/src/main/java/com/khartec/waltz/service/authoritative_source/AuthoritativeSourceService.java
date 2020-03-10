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

package com.khartec.waltz.service.authoritative_source;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.data.data_flow_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.authoritativesource.*;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
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
import static com.khartec.waltz.model.EntityKind.ORG_UNIT;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.AuthoritativeSource.AUTHORITATIVE_SOURCE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;


@Service
public class AuthoritativeSourceService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthoritativeSourceService.class);

    private final AuthoritativeSourceDao authoritativeSourceDao;
    private final DataTypeDao dataTypeDao;
    private final OrganisationalUnitDao organisationalUnitDao;
    private final ApplicationDao applicationDao;
    private final AuthSourceRatingCalculator ratingCalculator;
    private final ChangeLogService changeLogService;
    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Autowired
    public AuthoritativeSourceService(AuthoritativeSourceDao authoritativeSourceDao,
                                      DataTypeDao dataTypeDao,
                                      OrganisationalUnitDao organisationalUnitDao,
                                      ApplicationDao applicationDao,
                                      AuthSourceRatingCalculator ratingCalculator,
                                      ChangeLogService changeLogService,
                                      LogicalFlowDecoratorDao logicalFlowDecoratorDao) {
        checkNotNull(authoritativeSourceDao, "authoritativeSourceDao must not be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(organisationalUnitDao, "organisationalUnitDao cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(ratingCalculator, "ratingCalculator cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(logicalFlowDecoratorDao, "logicalFlowDecoratorDao cannot be null");

        this.authoritativeSourceDao = authoritativeSourceDao;
        this.dataTypeDao = dataTypeDao;
        this.organisationalUnitDao = organisationalUnitDao;
        this.applicationDao = applicationDao;
        this.ratingCalculator = ratingCalculator;
        this.changeLogService = changeLogService;
        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
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


    public int update(AuthoritativeSourceUpdateCommand command, String username) {
        int updateCount = authoritativeSourceDao.update(command);
        long authSourceId = command
                .id()
                .orElseThrow(() -> new IllegalArgumentException("cannot update an auth source without an id"));
        AuthoritativeSource updatedAuthSource = getById(authSourceId);
        ratingCalculator.update(updatedAuthSource.dataType(), updatedAuthSource.parentReference());
        logUpdate(command, username);
        return updateCount;
    }


    public int insert(AuthoritativeSourceCreateCommand command, String username) {
        int insertedCount = authoritativeSourceDao.insert(command);
        ratingCalculator.update(command.dataTypeId(), mkRef(ORG_UNIT, command.orgUnitId()));
        logInsert(command, username);
        return insertedCount;
    }


    public int remove(long id, String username) {

        logRemoval(id, username);
        AuthoritativeSource authSourceToDelete = getById(id);
        int deletedCount = authoritativeSourceDao.remove(id);
        ratingCalculator.update(authSourceToDelete.dataType(), authSourceToDelete.parentReference());
        return deletedCount;
    }


    public List<AuthoritativeSource> findAll() {
        return authoritativeSourceDao.findAll();
    }


    @Deprecated
    public boolean recalculateAllFlowRatings() {
        logicalFlowDecoratorDao.updateRatingsByCondition(AuthoritativenessRating.NO_OPINION, DSL.trueCondition());
        findAll()
                .forEach(authSource -> ratingCalculator.update(
                        authSource.dataType(),
                        authSource.parentReference()));
        return true;
    }


    public boolean fastRecalculateAllFlowRatings() {
        logicalFlowDecoratorDao.updateRatingsByCondition(AuthoritativenessRating.NO_OPINION, DSL.trueCondition());
        List<AuthoritativeRatingVantagePoint> authoritativeRatingVantagePoints = authoritativeSourceDao.findAuthoritativeRatingVantagePoints();
        authoritativeRatingVantagePoints.forEach(a -> {
            LOG.info("Updating decorators for: {}", a);
            int updateCount = logicalFlowDecoratorDao.updateDecoratorsForAuthSource(a);
            LOG.info("Updated {} decorators for: {}", updateCount, a);
        });
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


    public List<NonAuthoritativeSource> findNonAuthSources(IdSelectionOptions options) {
        Condition customSelectionCriteria;
        switch(options.entityReference().kind()) {
            case DATA_TYPE:
                GenericSelector dataTypeSelector = genericSelectorFactory.apply(options);
                customSelectionCriteria = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(dataTypeSelector.selector())
                    .and(AuthoritativeSourceDao.SUPPLIER_APP.KIND.notIn(options.filters().omitApplicationKinds()));
                break;

            case ORG_UNIT:
                GenericSelector orgUnitSelector = genericSelectorFactory.apply(options);
                customSelectionCriteria = AuthoritativeSourceDao.CONSUMER_APP.ORGANISATIONAL_UNIT_ID.in(orgUnitSelector.selector())
                    .and(AuthoritativeSourceDao.SUPPLIER_APP.KIND.notIn(options.filters().omitApplicationKinds()));
                break;

            case APP_GROUP:
            case FLOW_DIAGRAM:
            case MEASURABLE:
            case PERSON:
                customSelectionCriteria = mkConsumerSelectionCondition(options);
                break;

            default:
                throw new UnsupportedOperationException("Cannot calculate non-auth sources for ref" + options.entityReference());
        }

        return authoritativeSourceDao.findNonAuthSources(customSelectionCriteria);
    }


    public List<AuthoritativeSource> findAuthSources(IdSelectionOptions options) {
        Condition customSelectionCriteria;

        switch(options.entityReference().kind()) {
            case ORG_UNIT:
                GenericSelector orgUnitSelector = genericSelectorFactory.apply(options);
                customSelectionCriteria = AUTHORITATIVE_SOURCE.PARENT_ID.in(orgUnitSelector.selector())
                        .and(AuthoritativeSourceDao.SUPPLIER_APP.KIND.notIn(options.filters().omitApplicationKinds()));
                break;
            case DATA_TYPE:
                GenericSelector dataTypeSelector = genericSelectorFactory.apply(options);
                SelectConditionStep<Record1<String>> codeSelector = DSL
                        .select(DATA_TYPE.CODE)
                        .from(DATA_TYPE)
                        .where(DATA_TYPE.ID.in(dataTypeSelector.selector()));
                customSelectionCriteria = AUTHORITATIVE_SOURCE.DATA_TYPE.in(codeSelector)
                        .and(AuthoritativeSourceDao.SUPPLIER_APP.KIND.notIn(options.filters().omitApplicationKinds()));
                break;
            case APP_GROUP:
            case FLOW_DIAGRAM:
            case MEASURABLE:
            case PERSON:
                customSelectionCriteria = mkConsumerSelectionCondition(options);
                break;
            default:
                throw new UnsupportedOperationException("Cannot calculate auth sources for ref" + options.entityReference());
        }

        return authoritativeSourceDao.findAuthSources(customSelectionCriteria);

    }


    // -- HELPERS

    private Condition mkConsumerSelectionCondition(IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(options);
        return AuthoritativeSourceDao.CONSUMER_APP.ID.in(appIdSelector);
    }


    private void logRemoval(long id, String username) {
        AuthoritativeSource authSource = getById(id);
        if (authSource == null) {
            return;
        }

        OrganisationalUnit orgUnit = organisationalUnitDao.getById(authSource.parentReference().id());
        DataType dataType = dataTypeDao.getByCode(authSource.dataType());
        Application app = applicationDao.getById(authSource.applicationReference().id());


        if (app != null && dataType != null && orgUnit != null) {
            String msg = String.format(
                    "Removed %s as an authoritative source for type: %s for org: %s",
                    app.name(),
                    dataType.name(),
                    orgUnit.name());

            tripleLog(username, orgUnit, dataType, app, msg, Operation.REMOVE);
        }
    }



    private void logInsert(AuthoritativeSourceCreateCommand command, String username) {
        OrganisationalUnit orgUnit = organisationalUnitDao.getById(command.orgUnitId());
        DataType dataType = dataTypeDao.getById(command.dataTypeId());
        Application app = applicationDao.getById(command.applicationId());

        if (app != null && dataType != null && orgUnit != null) {
            String msg = String.format(
                    "Registered %s as an authoritative source for type: %s for org: %s",
                    app.name(),
                    dataType.name(),
                    orgUnit.name());

            tripleLog(username, orgUnit, dataType, app, msg, Operation.ADD);
        }
    }

    private void logUpdate(AuthoritativeSourceUpdateCommand command, String username) {
        AuthoritativeSource authSource = getById(command.id().get());
        if (authSource == null) {
            return;
        }

        OrganisationalUnit orgUnit = organisationalUnitDao.getById(authSource.parentReference().id());
        DataType dataType = dataTypeDao.getByCode(authSource.dataType());
        Application app = applicationDao.getById(authSource.applicationReference().id());

        if (app != null && dataType != null && orgUnit != null) {
            String msg = String.format(
                    "Updated %s as an authoritative source for type: %s for org: %s",
                    app.name(),
                    dataType.name(),
                    orgUnit.name());

            tripleLog(username, orgUnit, dataType, app, msg, Operation.UPDATE);
        }
    }


    private void tripleLog(String username, OrganisationalUnit orgUnit, DataType dataType, Application app, String msg, Operation operation) {
        ChangeLog ouLog = ImmutableChangeLog.builder()
                .message(msg)
                .severity(Severity.INFORMATION)
                .userId(username)
                .parentReference(mkRef(ORG_UNIT, orgUnit.id().get()))
                .childKind(EntityKind.APPLICATION)
                .operation(operation)
                .build();

        ChangeLog appLog = ImmutableChangeLog
                .copyOf(ouLog)
                .withParentReference(mkRef(EntityKind.APPLICATION, app.id().get()))
                .withChildKind(ORG_UNIT);

        ChangeLog dtLog = ImmutableChangeLog
                .copyOf(ouLog)
                .withParentReference(mkRef(EntityKind.DATA_TYPE, dataType.id().get()));

        changeLogService.write(ouLog);
        changeLogService.write(appLog);
        changeLogService.write(dtLog);
    }

}
