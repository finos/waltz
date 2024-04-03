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

package org.finos.waltz.service.data_flow_decorator;

import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.OrganisationalUnitIdProvider;
import org.finos.waltz.service.application.ApplicationService;
import org.finos.waltz.service.flow_classification_rule.FlowClassificationRuleResolver;
import org.finos.waltz.data.data_type.DataTypeDao;
import org.finos.waltz.data.flow_classification_rule.FlowClassificationDao;
import org.finos.waltz.data.flow_classification_rule.FlowClassificationRuleDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.ImmutableDataTypeDecorator;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.filter;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.utils.IdUtilities.indexById;

@Service
public class LogicalFlowDecoratorRatingsCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalFlowDecoratorRatingsCalculator.class);
    private static final Predicate<LogicalFlow> IS_APP_TO_APP_FLOW = f ->
                f.target().kind() == EntityKind.APPLICATION &&
                f.source().kind() == EntityKind.APPLICATION;

    private final ApplicationService applicationService;
    private final FlowClassificationDao flowClassificationDao;
    private final FlowClassificationRuleDao flowClassificationRuleDao;
    private final LogicalFlowDao logicalFlowDao;
    private final DataTypeDao dataTypeDao;


    @Autowired
    public LogicalFlowDecoratorRatingsCalculator(ApplicationService applicationService,
                                                 FlowClassificationDao flowClassificationDao,
                                                 FlowClassificationRuleDao flowClassificationRuleDao,
                                                 LogicalFlowDao logicalFlowDao,
                                                 DataTypeDao dataTypeDao) {
        checkNotNull(applicationService, "applicationService cannot be null");
        checkNotNull(flowClassificationDao, "flowClassificationDao cannot be null");
        checkNotNull(flowClassificationRuleDao, "flowClassificationRuleDao cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao cannot be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");

        this.applicationService = applicationService;
        this.flowClassificationDao = flowClassificationDao;
        this.flowClassificationRuleDao = flowClassificationRuleDao;
        this.logicalFlowDao = logicalFlowDao;
        this.dataTypeDao = dataTypeDao;
    }


    public Collection<DataTypeDecorator> calculate(Collection<DataTypeDecorator> decorators) {

        List<LogicalFlow> logicalFlows = loadFlows(decorators);

        List<Application> targetApps = loadTargetApplications(logicalFlows);
        List<Application> sourceApps = loadSourceApplications(logicalFlows);

        Map<Long, Application> targetAppsById = indexById(targetApps);
        Map<Long, Application> sourceAppsById = indexById(sourceApps);

        Map<Long, LogicalFlow> flowsById = indexById(logicalFlows);

        FlowClassificationRuleResolver outboundResolver = createResolver(FlowDirection.OUTBOUND, logicalFlows, targetApps);
        FlowClassificationRuleResolver inboundResolver = createResolver(FlowDirection.INBOUND, logicalFlows, sourceApps);

        return decorators
                .stream()
                .filter(d -> flowsById.containsKey(d.dataFlowId()))
                .map(decorator -> {
                    try {
                        if (decorator.decoratorEntity().kind() != EntityKind.DATA_TYPE) {
                            return decorator;
                        } else {
                            LogicalFlow flow = flowsById.get(decorator.dataFlowId());
                            EntityReference targetVantagePoint = lookupVantagePoint(targetAppsById, flow.target());
                            EntityReference sourceVantagePoint = lookupVantagePoint(sourceAppsById, flow.source());
                            Tuple2<AuthoritativenessRatingValue, Optional<Long>> sourceOutboundClassification = lookupClassification(
                                    targetVantagePoint,
                                    flow.target(),
                                    flow.source(),
                                    outboundResolver,
                                    decorator);
                            Tuple2<AuthoritativenessRatingValue, Optional<Long>> targetInboundClassification = lookupClassification(
                                    sourceVantagePoint,
                                    flow.source(),
                                    flow.target(),
                                    inboundResolver,
                                    decorator);
                            return ImmutableDataTypeDecorator
                                    .copyOf(decorator)
                                    .withRating(sourceOutboundClassification.v1)
                                    .withFlowClassificationRuleId(sourceOutboundClassification.v2)
                                    .withTargetInboundRating(targetInboundClassification.v1)
                                    .withInboundFlowClassificationRuleId(targetInboundClassification.v2);
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
        Set<Long> targetApplicationIds = flows
                .stream()
                .filter(df -> df.target().kind().equals(EntityKind.APPLICATION))
                .map(df -> df.target().id())
                .collect(Collectors.toSet());

        return applicationService
                .findByIds(targetApplicationIds);
    }

    private List<Application> loadSourceApplications(List<LogicalFlow> flows) {
        Set<Long> sourceApplicationIds = flows
                .stream()
                .filter(df -> df.source().kind().equals(EntityKind.APPLICATION))
                .map(df -> df.source().id())
                .collect(Collectors.toSet());

        return applicationService
                .findByIds(sourceApplicationIds);
    }


    private List<LogicalFlow> loadFlows(Collection<DataTypeDecorator> decorators) {
        Set<Long> dataFlowIds = map(decorators, DataTypeDecorator::dataFlowId);
        return logicalFlowDao.findActiveByFlowIds(dataFlowIds);
    }


    private FlowClassificationRuleResolver createResolver(FlowDirection direction,
                                                          List<LogicalFlow> logicalFlows,
                                                          Collection<Application> apps) {

        Set<EntityReference> vantagePointEntityLookups = map(
                logicalFlows,
                d -> direction.equals(FlowDirection.OUTBOUND) ? d.target() : d.source());

        Set<Long> appVantagePoints = getVantagePointIdsForKind(vantagePointEntityLookups, EntityKind.APPLICATION);
        Set<Long> actorVantagePoints = getVantagePointIdsForKind(vantagePointEntityLookups, EntityKind.ACTOR);

        // apps are targets for the rule
        Set<Long> orgIds = map(apps, OrganisationalUnitIdProvider::organisationalUnitId);

        // this brings back many expanded vantage points
        List<FlowClassificationRuleVantagePoint> flowClassificationRuleVantagePoints =
                flowClassificationRuleDao.findExpandedFlowClassificationRuleVantagePoints(direction, orgIds, appVantagePoints, actorVantagePoints);

        return new FlowClassificationRuleResolver(direction, flowClassificationRuleVantagePoints);
    }

    private Set<Long> getVantagePointIdsForKind(Set<EntityReference> vantagePointEntityLookups, EntityKind entityKind) {
        return vantagePointEntityLookups
                .stream()
                .filter(d -> d.kind().equals(entityKind))
                .map(EntityReference::id)
                .collect(Collectors.toSet());
    }


    private Tuple2<AuthoritativenessRatingValue, Optional<Long>> lookupClassification(EntityReference vantagePointOrgUnit,
                                                                                      EntityReference vantagePointEntity,
                                                                                      EntityReference subject,
                                                                                      FlowClassificationRuleResolver resolver,
                                                                                      DataTypeDecorator decorator) {

        return resolver.resolve(vantagePointOrgUnit, vantagePointEntity, subject, decorator.decoratorEntity().id());
    }

//
//    private Optional<Long> lookupFlowClassificationRule(Map<Long, DataType> typesById,
//                                                        Map<Long, LogicalFlow> flowsById,
//                                                        Map<Long, Application> targetAppsById,
//                                                        FlowClassificationRuleResolver resolver,
//                                                        DataTypeDecorator decorator) {
//        LogicalFlow flow = flowsById.get(decorator.dataFlowId());
//        EntityReference vantagePoint = lookupVantagePoint(targetAppsById, flow);
//        EntityReference source = flow.source();
//
//        Optional<FlowClassificationRuleVantagePoint> flowClassificationRuleVantagePoint = resolver.resolveAuthSource(vantagePoint, source, decorator.dataTypeId());
//
//        return flowClassificationRuleVantagePoint
//                .map(FlowClassificationRuleVantagePoint::ruleId);
//    }


    // Vantage point needs to be included in lookup of both point to point flows and org unit flows. So need ot pass in the app and then offer both the OU and App lookup
    private EntityReference lookupVantagePoint(Map<Long, Application> appsById, EntityReference lookupEntity) {
        Application app = appsById.get(lookupEntity.id());

        if (app != null) {
            long targetOrgUnitId = app.organisationalUnitId();
            return EntityReference.mkRef(
                    EntityKind.ORG_UNIT,
                    targetOrgUnitId);
        } else {
            return null;
        }
    }


    private String lookupDataTypeCode(Map<Long, DataType> typesById, DataTypeDecorator decorator) {
        long dataTypeId = decorator.decoratorEntity().id();
        return typesById.get(dataTypeId).code();
    }


}
