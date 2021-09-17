import {remote} from "./remote";


export function mkAssessmentRatingStore() {
    const findByDefinitionId = (id) => remote
        .fetchViewData(
            "GET",
            `api/assessment-rating/definition-id/${id}`);

    const findByEntityKind = (kind, force = false) => remote
        .fetchViewList(
            "GET",
            `api/assessment-rating/entity-kind/${kind}`,
            null,
            {force});

    return {
        findByDefinitionId,
        findByEntityKind
    };
}


export const assessmentRatingStore = mkAssessmentRatingStore();