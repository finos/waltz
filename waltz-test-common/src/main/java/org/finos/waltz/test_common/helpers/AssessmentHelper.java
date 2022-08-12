package org.finos.waltz.test_common.helpers;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.assessment_definition.AssessmentVisibility;
import org.finos.waltz.model.assessment_definition.ImmutableAssessmentDefinition;
import org.finos.waltz.model.assessment_rating.ImmutableSaveAssessmentRatingCommand;
import org.finos.waltz.model.assessment_rating.SaveAssessmentRatingCommand;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;

@Service
public class AssessmentHelper {

    @Autowired
    private AssessmentDefinitionService definitionService;

    @Autowired
    private AssessmentRatingService ratingService;


    public long createDefinition(long schemeId, String name, String permittedRole, AssessmentVisibility visibility, String definitionGroup) {

        ImmutableAssessmentDefinition.Builder def = ImmutableAssessmentDefinition.builder()
                .name(name)
                .description("desc")
                .isReadOnly(false)
                .externalId(mkName(name))
                .entityKind(EntityKind.APPLICATION)
                .lastUpdatedBy("test")
                .visibility(visibility)
                .ratingSchemeId(schemeId);

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

}
