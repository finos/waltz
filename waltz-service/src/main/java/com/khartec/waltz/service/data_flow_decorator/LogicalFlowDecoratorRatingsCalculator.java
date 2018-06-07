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

package com.khartec.waltz.service.data_flow_decorator;

import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.authoritativesource.AuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.data_flow_decorator.ImmutableLogicalFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecorator;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.filter;
import static com.khartec.waltz.common.ListUtilities.isEmpty;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.model.utils.IdUtilities.indexById;

@Service
public class LogicalFlowDecoratorRatingsCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalFlowDecoratorRatingsCalculator.class);
    private static final Predicate<LogicalFlow> IS_APP_TO_APP_FLOW = f ->
                f.target().kind() == EntityKind.APPLICATION &&
                f.source().kind() == EntityKind.APPLICATION;

    private final ApplicationService applicationService;
    private final AuthoritativeSourceDao authoritativeSourceDao;
    private final LogicalFlowDao logicalFlowDao;
    private final DataTypeDao dataTypeDao;


    @Autowired
    public LogicalFlowDecoratorRatingsCalculator(ApplicationService applicationService,
                                                 AuthoritativeSourceDao authoritativeSourceDao,
                                                 LogicalFlowDao logicalFlowDao,
                                                 DataTypeDao dataTypeDao) {
        checkNotNull(applicationService, "applicationService cannot be null");
        checkNotNull(authoritativeSourceDao, "authoritativeSourceDao cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao cannot be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");

        this.applicationService = applicationService;
        this.authoritativeSourceDao = authoritativeSourceDao;
        this.logicalFlowDao = logicalFlowDao;
        this.dataTypeDao = dataTypeDao;
    }


    public Collection<LogicalFlowDecorator> calculate(Collection<LogicalFlowDecorator> decorators) {

        List<LogicalFlow> appToAppFlows = filter(
                IS_APP_TO_APP_FLOW,
                loadFlows(decorators));

        if (isEmpty(appToAppFlows)) return Collections.emptyList();

        List<Application> targetApps = loadTargetApplications(appToAppFlows);
        List<DataType> dataTypes = dataTypeDao.getAll();

        Map<Long, DataType> typesById = indexById(dataTypes);
        Map<Long, LogicalFlow> flowsById = indexById(appToAppFlows);
        Map<Long, Application> targetAppsById = indexById(targetApps);

        AuthoritativeSourceResolver resolver = createResolver(targetApps);

        return decorators.stream()
                .filter(d -> flowsById.containsKey(d.dataFlowId()))
                .map(decorator -> {
                    try {
                        if (decorator.decoratorEntity().kind() != EntityKind.DATA_TYPE) {
                            return decorator;
                        } else {
                            AuthoritativenessRating rating = lookupRating(
                                    typesById,
                                    flowsById,
                                    targetAppsById,
                                    resolver,
                                    decorator);
                            return ImmutableLogicalFlowDecorator
                                    .copyOf(decorator)
                                    .withRating(rating);
                        }
                    } catch (Exception e) {
                        LOG.warn("Failed to calculate rating for decorator: {}, reason: {}", decorator, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }


    private List<Application> loadTargetApplications(List<LogicalFlow> flows) {
        Set<Long> targetApplicationIds = map(
                flows,
                df -> df.target().id());

        return applicationService
                .findByIds(targetApplicationIds);
    }


    private List<LogicalFlow> loadFlows(Collection<LogicalFlowDecorator> decorators) {
        Set<Long> dataFlowIds = map(decorators, d -> d.dataFlowId());
        return logicalFlowDao.findByFlowIds(dataFlowIds);
    }


    private AuthoritativeSourceResolver createResolver(Collection<Application> targetApps) {
        Set<Long> orgIds = map(targetApps, app -> app.organisationalUnitId());

        List<AuthoritativeRatingVantagePoint> authoritativeRatingVantagePoints =
                authoritativeSourceDao.findAuthoritativeRatingVantagePoints(orgIds);

        AuthoritativeSourceResolver resolver = new AuthoritativeSourceResolver(authoritativeRatingVantagePoints);
        return resolver;
    }


    private AuthoritativenessRating lookupRating(Map<Long, DataType> typesById,
                                                 Map<Long, LogicalFlow> flowsById,
                                                 Map<Long, Application> targetAppsById,
                                                 AuthoritativeSourceResolver resolver,
                                                 LogicalFlowDecorator decorator) {
        LogicalFlow flow = flowsById.get(decorator.dataFlowId());

        EntityReference vantagePoint = lookupVantagePoint(targetAppsById, flow);
        EntityReference source = flow.source();
        String dataTypeCode = lookupDataTypeCode(typesById, decorator);

        return resolver.resolve(vantagePoint, source, dataTypeCode);
    }


    private EntityReference lookupVantagePoint(Map<Long, Application> targetAppsById, LogicalFlow flow) {
        Application targetApp = targetAppsById.get(flow.target().id());
        long targetOrgUnitId = targetApp.organisationalUnitId();

        return EntityReference.mkRef(
                EntityKind.ORG_UNIT,
                targetOrgUnitId);
    }


    private String lookupDataTypeCode(Map<Long, DataType> typesById, LogicalFlowDecorator decorator) {
        long dataTypeId = decorator.decoratorEntity().id();
        return typesById.get(dataTypeId).code();
    }


}
