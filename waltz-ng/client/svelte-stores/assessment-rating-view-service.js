import {remote} from "./remote";


export function mkAssessmentRatingViewStore() {

    const findEntitiesGroupedByDefinitionAndOutcome = (entityKind, entityIds = [], force = false) => remote
        .fetchViewList(
            "POST",
            `api/assessment-rating-view/kind/${entityKind}/grouped`,
            entityIds,
            [],
            {force});

    return {
        findEntitiesGroupedByDefinitionAndOutcome,
    };
}


export const assessmentRatingViewStore = mkAssessmentRatingViewStore();