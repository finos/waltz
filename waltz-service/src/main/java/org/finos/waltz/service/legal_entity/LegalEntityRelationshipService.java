package org.finos.waltz.service.legal_entity;

import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.legal_entity.LegalEntityRelationshipDao;
import org.finos.waltz.data.legal_entity.LegalEntityRelationshipIdSelectorFactory;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.legal_entity.*;
import org.finos.waltz.schema.tables.records.ChangeLogRecord;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.map;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class LegalEntityRelationshipService {

    private final LegalEntityRelationshipDao legalEntityRelationshipDao;
    private static final Logger LOG = LoggerFactory.getLogger(LegalEntityRelationshipService.class);

    private final LegalEntityRelationshipIdSelectorFactory legalEntityRelationshipIdSelectorFactory = new LegalEntityRelationshipIdSelectorFactory();

    @Autowired
    public LegalEntityRelationshipService(LegalEntityRelationshipDao legalEntityRelationshipDao) {

        checkNotNull(legalEntityRelationshipDao, "legalEntityRelationshipDao cannot be null");

        this.legalEntityRelationshipDao = legalEntityRelationshipDao;
    }


    public LegalEntityRelationship getById(long id) {
        return legalEntityRelationshipDao.getById(id);
    }

    public Set<LegalEntityRelationship> findByLegalEntityId(long legalEntityId) {
        return legalEntityRelationshipDao.findByLegalEntityId(legalEntityId);
    }

    public Set<LegalEntityRelationship> findByEntityReference(EntityReference ref) {
        return legalEntityRelationshipDao.findByEntityReference(ref);
    }

    public Set<LegalEntityRelationship> findByRelationshipKindId(DSLContext tx, long relKindId) {
        return legalEntityRelationshipDao.findByRelationshipKind(tx, relKindId);
    }

    public Set<LegalEntityRelationship> findByRelationshipKindId(long relationshipKindId) {
        return legalEntityRelationshipDao.findByRelationshipKind(null, relationshipKindId);
    }

    public int bulkAdd(DSLContext tx, Set<LegalEntityRelationship> relationshipsToAdd, String username) {

        mkAdditionChangeLogs(relationshipsToAdd);

        return legalEntityRelationshipDao.bulkAdd(tx, relationshipsToAdd);
    }

    private void mkAdditionChangeLogs(Set<LegalEntityRelationship> relationshipsToAdd) {
        map(relationshipsToAdd, d -> mkChangeLog(d, Operation.ADD));
    }

    private ChangeLogRecord mkChangeLog(LegalEntityRelationship relationship, Operation operation) {

        return null;
    }

    public int bulkUpdate(DSLContext tx, Set<LegalEntityRelationship> relationshipsToUpdate, String username) {
        map(relationshipsToUpdate, d -> mkChangeLog(d, Operation.UPDATE));
        return legalEntityRelationshipDao.bulkUpdate(tx, relationshipsToUpdate);
    }

    public int bulkRemove(DSLContext tx, Set<LegalEntityRelationship> relationships, String username) {
        map(relationships, d -> mkChangeLog(d, Operation.REMOVE));
        return legalEntityRelationshipDao.bulkRemove(tx, relationships);
    }

    public LegalEntityRelationshipView getViewByRelKindAndSelector(long relKindId, IdSelectionOptions selectionOptions) {

        Select<Record1<Long>> relSelector = legalEntityRelationshipIdSelectorFactory.apply(selectionOptions);

        Set<LegalEntityRelationship> relationships = legalEntityRelationshipDao.findByRelationshipKindAndTargetSelector(relKindId, relSelector);
        Set<LegalEntityRelationshipAssessmentInfo> assessmentInfo = legalEntityRelationshipDao.getViewAssessmentsByRelKind(relKindId, relSelector);

        Set<EntityReference> headerAssessments = map(assessmentInfo, LegalEntityRelationshipAssessmentInfo::definitionRef);

        Set<LegalEntityRelationshipViewRow> rows = mkLegalEntityRelationshipViewRows(relationships, assessmentInfo);

        return ImmutableLegalEntityRelationshipView
                .builder()
                .assessmentHeaders(headerAssessments)
                .rows(rows)
                .build();
    }


    private Set<LegalEntityRelationshipViewRow> mkLegalEntityRelationshipViewRows(Set<LegalEntityRelationship> relationships,
                                                                                  Set<LegalEntityRelationshipAssessmentInfo> assessmentInfo) {

        Map<Long, Set<LegalEntityRelationshipViewAssessment>> assessmentsByRelId = groupBy(
                assessmentInfo,
                LegalEntityRelationshipAssessmentInfo::relationshipId)
                .entrySet()
                .stream()
                .map(kv -> tuple(kv.getKey(), getAssessments(kv.getValue())))
                .collect(Collectors.toMap(t -> t.v1, t -> t.v2));

        return map(
                relationships,
                d -> ImmutableLegalEntityRelationshipViewRow.builder()
                        .relationship(d)
                        .assessments(assessmentsByRelId.getOrDefault(d.entityReference().id(), Collections.emptySet()))
                        .build());
    }


    private Set<LegalEntityRelationshipViewAssessment> getAssessments(Collection<LegalEntityRelationshipAssessmentInfo> assessmentsForRel) {

        return groupBy(
                assessmentsForRel,
                d -> d.definitionRef().id(),
                LegalEntityRelationshipAssessmentInfo::ratingId)
                .entrySet()
                .stream()
                .map(kv -> ImmutableLegalEntityRelationshipViewAssessment.builder()
                        .assessmentDefinitionId(kv.getKey())
                        .ratingIds(kv.getValue())
                        .build())
                .collect(toSet());
    }

}
