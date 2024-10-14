package org.finos.waltz.test_common.helpers;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.Cardinality;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentDefinition;
import org.finos.waltz.model.assessment_rating.ImmutableSaveAssessmentRatingCommand;
import org.finos.waltz.model.assessment_rating.SaveAssessmentRatingCommand;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.schema.Tables.ASSESSMENT_DEFINITION;
import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;

@Service
public class AssessmentHelper {

    @Autowired
    private DSLContext dsl;

    @Autowired
    private AssessmentDefinitionService definitionService;

    @Autowired
    private AssessmentRatingService ratingService;


    public long createDefinition(long schemeId, String name, String permittedRole, AssessmentVisibility visibility, String definitionGroup) {
        return createDefinition(schemeId, name, permittedRole, visibility, definitionGroup, EntityKind.APPLICATION, Cardinality.ZERO_ONE, Optional.empty());
    }

    public long createDefinition(long schemeId, String name, String permittedRole, AssessmentVisibility visibility, String definitionGroup, EntityKind entityKind, EntityReference qualifierRef) {
        return createDefinition(schemeId, name, permittedRole, visibility, definitionGroup, entityKind, Cardinality.ZERO_ONE, Optional.ofNullable(qualifierRef));
    }

    public long createDefinition(long schemeId, String name, String permittedRole, AssessmentVisibility visibility, String definitionGroup, EntityKind entityKind, Cardinality cardinality, Optional<EntityReference> qualifierRef) {

        ImmutableAssessmentDefinition.Builder def = ImmutableAssessmentDefinition.builder()
                .name(name)
                .description("desc")
                .isReadOnly(false)
                .externalId(mkName(name))
                .entityKind(entityKind)
                .lastUpdatedBy("test")
                .visibility(visibility)
                .cardinality(cardinality)
                .ratingSchemeId(schemeId)
                .qualifierReference(qualifierRef);

        if (!isEmpty(permittedRole)) {
            def.permittedRole(permittedRole);
        }

        if (!isEmpty(definitionGroup)) {
            def.definitionGroup(definitionGroup);
        }

        long defId = definitionService.save(def.build());

        return defId;
    }


    public void createAssessment(Long defId, EntityReference ref, Long ratingId, String username) throws InsufficientPrivelegeException {

        SaveAssessmentRatingCommand cmd = ImmutableSaveAssessmentRatingCommand
                .builder()
                .assessmentDefinitionId(defId)
                .entityReference(ref)
                .ratingId(ratingId)
                .lastUpdatedBy(username)
                .build();

        ratingService.store(cmd, username);
    }


    public void createAssessment(Long defId, EntityReference ref, Long ratingId) {
        AssessmentRatingRecord record = dsl.newRecord(ASSESSMENT_RATING);
        record.setAssessmentDefinitionId(defId);
        record.setEntityId(ref.id());
        record.setEntityKind(ref.kind().name());
        record.setRatingId(ratingId);
        record.setDescription("test");
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy("test");
        record.setIsReadonly(false);
        record.setProvenance("test");

        dsl
                .insertInto(ASSESSMENT_RATING)
                .set(record)
                .onDuplicateKeyIgnore()
                .execute();
    }


    public void updateDefinitionReadOnly(long defnId) {
        dsl
                .update(ASSESSMENT_DEFINITION)
                .set(ASSESSMENT_DEFINITION.IS_READONLY, true)
                .where(ASSESSMENT_DEFINITION.ID.eq(defnId))
                .execute();
    }


    public void updateRatingReadOnly(EntityReference ref, long defnId) {
        dsl
                .update(ASSESSMENT_RATING)
                .set(ASSESSMENT_RATING.IS_READONLY, true)
                .where(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(defnId)
                        .and(ASSESSMENT_RATING.ENTITY_KIND.eq(ref.kind().name())
                                .and(ASSESSMENT_RATING.ENTITY_ID.eq(ref.id()))))
                .execute();
    }


    public void setDefExtId(long assessmentDefinitionId,
                            String externalId) {
        dsl.update(ASSESSMENT_DEFINITION)
                .set(ASSESSMENT_DEFINITION.EXTERNAL_ID, externalId)
                .where(ASSESSMENT_DEFINITION.ID.eq(assessmentDefinitionId))
                .execute();
    }
}
