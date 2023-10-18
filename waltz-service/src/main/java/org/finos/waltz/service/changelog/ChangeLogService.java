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

package org.finos.waltz.service.changelog;

import org.finos.waltz.data.EntityReferenceNameResolver;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.changelog.ChangeLogDao;
import org.finos.waltz.data.changelog.ChangeLogSummariesDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.measurable_rating.MeasurableRatingDao;
import org.finos.waltz.data.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommissionDao;
import org.finos.waltz.data.measurable_rating_replacement.MeasurableRatingReplacementDao;
import org.finos.waltz.data.physical_flow.PhysicalFlowDao;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.tally.DateTally;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.model.EntityKind.*;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.EntityReferenceUtilities.safeName;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Service
public class ChangeLogService {

    private final ChangeLogDao changeLogDao;
    private final ChangeLogSummariesDao changeLogSummariesDao;
    private final PhysicalFlowDao physicalFlowDao;
    private final LogicalFlowDao logicalFlowDao;
    private final PhysicalSpecificationDao physicalSpecificationDao;
    private final ApplicationDao applicationDao;
    private final MeasurableRatingReplacementDao measurableRatingReplacementdao;
    private final MeasurableRatingDao measurableRatingDao;
    private final MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao;
    private final EntityReferenceNameResolver nameResolver;


    @Autowired
    public ChangeLogService(ChangeLogDao changeLogDao,
                            ChangeLogSummariesDao changeLogSummariesDao,
                            PhysicalFlowDao physicalFlowDao,
                            PhysicalSpecificationDao physicalSpecificationDao,
                            LogicalFlowDao logicalFlowDao,
                            ApplicationDao applicationDao,
                            MeasurableRatingReplacementDao measurableRatingReplacementDao,
                            MeasurableRatingDao measurableRatingdao,
                            MeasurableRatingPlannedDecommissionDao measurableRatingPlannedDecommissionDao,
                            EntityReferenceNameResolver nameResolver) {
        checkNotNull(changeLogDao, "changeLogDao must not be null");
        checkNotNull(changeLogSummariesDao, "changeLogSummariesDao must not be null");
        checkNotNull(physicalFlowDao, "physicalFlowDao cannot be null");
        checkNotNull(physicalSpecificationDao, "physicalSpecificationDao cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao cannot be null");
        checkNotNull(measurableRatingdao, "measurableRatingdao cannot be null");
        checkNotNull(measurableRatingReplacementDao, "measurableRatingReplacementDao cannot be null");
        checkNotNull(measurableRatingPlannedDecommissionDao, "measurableRatingPlannedDecommissionDao cannot be null");
        checkNotNull(nameResolver, "nameResolver cannot be null");

        this.changeLogDao = changeLogDao;
        this.changeLogSummariesDao = changeLogSummariesDao;
        this.physicalFlowDao = physicalFlowDao;
        this.physicalSpecificationDao = physicalSpecificationDao;
        this.logicalFlowDao = logicalFlowDao;
        this.applicationDao = applicationDao;
        this.measurableRatingDao = measurableRatingdao;
        this.measurableRatingReplacementdao = measurableRatingReplacementDao;
        this.measurableRatingPlannedDecommissionDao = measurableRatingPlannedDecommissionDao;
        this.nameResolver = nameResolver;
    }


    public List<ChangeLog> findByParentReferenceForDateRange(EntityReference ref,
                                                             Date startDate,
                                                             Date endDate,
                                                             Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");
        return changeLogDao.findByParentReferenceForDateRange(ref, startDate, endDate, limit);
    }


    public List<ChangeLog> findByPersonReferenceForDateRange(EntityReference ref,
                                                             Date startDate,
                                                             Date endDate,
                                                             Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");
        return changeLogDao.findByPersonReferenceForDateRange(ref, startDate, endDate, limit);
    }

    public List<ChangeLog> findByParentReference(EntityReference ref,
                                                 Optional<java.util.Date> date,
                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");
        return changeLogDao.findByParentReference(ref, date, limit);
    }


    public List<ChangeLog> findByPersonReference(EntityReference ref,
                                                 Optional<java.util.Date> date,
                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");
        return changeLogDao.findByPersonReference(ref, date, limit);
    }


    public List<ChangeLog> findByUser(String userName,
                                      Optional<Integer> limit) {
        checkNotEmpty(userName, "Username cannot be empty");
        return changeLogDao.findByUser(userName, limit);
    }


    public int write(ChangeLog changeLog) {
        return changeLogDao.write(changeLog);
    }


    public int[] write(Collection<ChangeLog> changeLogs) {
        return changeLogDao.write(changeLogs);
    }


    /**
     * Given an entity ref this function will determine all changelog entries made _after_ the latest
     * attestations for that entity.  Change log is matched between the attestation kind and the change
     * log child kind.
     *
     * @param ref  target reference
     * @return list of changes (empty if no attestations or if no changes)
     */
    public List<ChangeLog> findUnattestedChanges(EntityReference ref) {
        return changeLogDao.findUnattestedChanges(ref);
    }


    public void writeChangeLogEntries(EntityReference ref, String userId, String postamble, Operation operation) {
        switch (ref.kind()) {
            case PHYSICAL_FLOW:
                PhysicalFlow physicalFlow = physicalFlowDao.getById(ref.id());
                writeChangeLogEntries(physicalFlow, userId, postamble, operation);
                break;
            case PHYSICAL_SPECIFICATION:
                PhysicalSpecification physicalSpec = physicalSpecificationDao.getById(ref.id());
                writeChangeLogEntries(physicalSpec, userId, postamble, operation);
                break;
            case LOGICAL_DATA_FLOW:
                LogicalFlow logicalFlow = logicalFlowDao.getByFlowId(ref.id());
                writeChangeLogEntries(logicalFlow, userId, postamble, operation);
                break;
            case MEASURABLE_RATING_REPLACEMENT:
                MeasurableRatingReplacement measurableRatingReplacement = measurableRatingReplacementdao.getById(ref.id());
                writeChangeLogEntries(measurableRatingReplacement, userId, postamble, operation);
                break;
            case MEASURABLE_RATING_PLANNED_DECOMMISSION:
                MeasurableRatingPlannedDecommission measurableRatingPlannedDecommission = measurableRatingPlannedDecommissionDao.getById(ref.id());
                writeChangeLogEntries(measurableRatingPlannedDecommission, userId, postamble, operation);
            default:
                // nop
        }
    }

    public void writeChangeLogEntries(LogicalFlow logicalFlow,
                                      String userId,
                                      String postamble,
                                      Operation operation) {
        Tuple2<String, Set<EntityReference>> t = preparePreambleAndEntitiesForChangeLogs(logicalFlow);
        String message = format("%s: %s", t.v1, postamble);
        writeChangeLogEntries(t.v2, message, operation, logicalFlow.entityReference(), userId);
    }


    public void writeChangeLogEntries(PhysicalFlow physicalFlow,
                                      String userId,
                                      String postamble,
                                      Operation operation) {
        Tuple2<String, Set<EntityReference>> t = preparePreambleAndEntitiesForChangeLogs(physicalFlow);
        String message = format("%s: %s", t.v1, postamble);
        writeChangeLogEntries(t.v2, message, operation, physicalFlow.entityReference(), userId);
    }


    public void writeChangeLogEntries(PhysicalSpecification physicalSpec,
                                      String userId,
                                      String postamble,
                                      Operation operation) {
        Tuple2<String, Set<EntityReference>> t = preparePreambleAndEntitiesForChangeLogs(physicalSpec);
        String message = format("%s: %s", t.v1, postamble);
        writeChangeLogEntries(t.v2, message, operation, physicalSpec.entityReference(), userId);
    }

    public void writeChangeLogEntries(MeasurableRatingReplacement measurableRatingReplacement,
                                      String userId,
                                      String postamble,
                                      Operation operation){
        Tuple2<String, Set<EntityReference>> t = preparePreambleAndEntitiesForChangeLogs(measurableRatingReplacement);
        String message = format("%s: %s", t.v1, postamble);
        writeChangeLogEntries(t.v2, message, operation, MEASURABLE_RATING_REPLACEMENT, userId);
    }


    public void writeChangeLogEntries(MeasurableRatingPlannedDecommission measurableRatingPlannedDecommission,
                                      String userId,
                                      String postamble,
                                      Operation operation){
        Tuple2<String, Set<EntityReference>> t = preparePreambleAndEntitiesForChangeLogs(measurableRatingPlannedDecommission);
        String message = format("%s: %s", t.v1, postamble);
        writeChangeLogEntries(t.v2, message, operation, MEASURABLE_RATING_PLANNED_DECOMMISSION, userId);
    }


    public List<DateTally> findCountByDateForParentKindBySelector(EntityKind parentKind,
                                                                  IdSelectionOptions selectionOptions,
                                                                  Optional<Integer> limit) {
        GenericSelector genericSelector = new GenericSelectorFactory().applyForKind(parentKind, selectionOptions);
        return changeLogSummariesDao.findCountByDateForParentKindBySelector(genericSelector, limit);
    }


    private void writeChangeLogEntries(Set<EntityReference> refs,
                                       String message,
                                       Operation operation,
                                       EntityKind childKind,
                                       String userId) {
        Set<ChangeLog> changeLogEntries = map(
                refs,
                r -> ImmutableChangeLog
                        .builder()
                        .parentReference(r)
                        .message(message)
                        .severity(Severity.INFORMATION)
                        .userId(userId)
                        .childKind(childKind)
                        .operation(operation)
                        .build());

        changeLogDao.write(changeLogEntries);
    }


    private void writeChangeLogEntries(Set<EntityReference> refs,
                                       String message,
                                       Operation operation,
                                       EntityReference childRef,
                                       String userId) {
        Set<ChangeLog> changeLogEntries = map(
                refs,
                r -> ImmutableChangeLog
                        .builder()
                        .parentReference(r)
                        .message(message)
                        .severity(Severity.INFORMATION)
                        .userId(userId)
                        .childKind(childRef.kind())
                        .childId(childRef.id())
                        .operation(operation)
                        .build());

        changeLogDao.write(changeLogEntries);
    }


    private Tuple2<String, Set<EntityReference>> preparePreambleAndEntitiesForChangeLogs(PhysicalSpecification physicalSpec) {
        List<PhysicalFlow> physicalFlows = physicalFlowDao.findBySpecificationId(physicalSpec.id().get());

        String messagePreamble = format(
                "Physical spec: %s",
                physicalSpec.name());

        return tuple(
                messagePreamble,
                union(
                    map(physicalFlows, PhysicalFlow::entityReference),
                    asSet(physicalSpec.entityReference())));

    }


    private Tuple2<String, Set<EntityReference>> preparePreambleAndEntitiesForChangeLogs(PhysicalFlow physicalFlow) {
        LogicalFlow logicalFlow = logicalFlowDao.getByFlowId(physicalFlow.logicalFlowId());
        PhysicalSpecification specification = physicalSpecificationDao.getById(physicalFlow.specificationId());

        String messagePreamble = format(
                "Physical flow: %s, from: %s, to: %s",
                specification.name(),
                safeName(logicalFlow.source()),
                safeName(logicalFlow.target()));

        return tuple(
                messagePreamble,
                asSet(
                        physicalFlow.entityReference(),
                        logicalFlow.entityReference(),
                        logicalFlow.source(),
                        logicalFlow.target()));

    }


    private Tuple2<String, Set<EntityReference>> preparePreambleAndEntitiesForChangeLogs(LogicalFlow logicalFlow) {
        String messagePreamble = format(
                "Logical flow from: %s, to: %s",
                safeName(logicalFlow.source()),
                safeName(logicalFlow.target()));


        return tuple(
                messagePreamble,
                asSet(
                        logicalFlow.entityReference(),
                        logicalFlow.source(),
                        logicalFlow.target()));
    }


    private Tuple2<String, Set<EntityReference>> preparePreambleAndEntitiesForChangeLogs(MeasurableRatingReplacement measurableRatingReplacement) {

        MeasurableRatingPlannedDecommission plannedDecommission = measurableRatingPlannedDecommissionDao.getById(measurableRatingReplacement.decommissionId());
        MeasurableRating rating = measurableRatingDao.getById(plannedDecommission.measurableRatingId());
        String measurableName = resolveName(rating.measurableId(), MEASURABLE);
        String originalEntityName = resolveName(rating.entityReference().id(), rating.entityReference().kind());
        String newEntityName = resolveName(measurableRatingReplacement.entityReference().id(), measurableRatingReplacement.entityReference().kind());

        String messagePreamble = format(
                "Replacement %s: %s [%d], for measurable: %s [%d] on: %s [%d]",
                measurableRatingReplacement.entityReference().kind().name().toLowerCase(),
                newEntityName,
                measurableRatingReplacement.entityReference().id(),
                measurableName,
                rating.measurableId(),
                originalEntityName,
                rating.entityReference().id());

        return tuple(
                messagePreamble,
                asSet(measurableRatingReplacement.entityReference(), rating.entityReference()));

    }


    private Tuple2<String, Set<EntityReference>> preparePreambleAndEntitiesForChangeLogs(MeasurableRatingPlannedDecommission measurableRatingPlannedDecommission) {

        MeasurableRating rating = measurableRatingDao.getById(measurableRatingPlannedDecommission.measurableRatingId());
        Set<MeasurableRatingReplacement> replacements = measurableRatingReplacementdao.fetchByDecommissionId(measurableRatingPlannedDecommission.id());
        String measurableName = resolveName(rating.measurableId(), MEASURABLE);
        EntityReference entityReference = rating.entityReference();
        String entityName = resolveName(entityReference.id(), entityReference.kind());

        String messagePreamble = format(
                "Measurable Rating: %s [%d] on: %s [%s]",
                measurableName,
                rating.measurableId(),
                entityName,
                getExternalId(entityReference)
                        .map(ExternalIdValue::value)
                        .orElse(String.valueOf(entityReference.id())));

        return tuple(
                messagePreamble,
                union(map(replacements, MeasurableRatingReplacement::entityReference), asSet(entityReference)));
    }


    private Optional<ExternalIdValue> getExternalId(EntityReference entityReference) {
        return entityReference.kind().equals(APPLICATION)
                ? applicationDao.getById(entityReference.id()).assetCode()
                : Optional.empty();
    }


    private String resolveName(long id, EntityKind kind) {
        return nameResolver
                .resolve(mkRef(kind, id))
                .flatMap(EntityReference::name)
                .orElse("UNKNOWN");
    }
}
