import {remote} from "./remote";


export function mkAssessmentRatingStore() {
    const findByDefinitionId = (id) => remote
        .fetchViewData(
            "GET",
            `api/assessment-rating/definition-id/${id}`);

    return {
        findByDefinitionId
    };
}


export const assessmentRatingStore = mkAssessmentRatingStore();