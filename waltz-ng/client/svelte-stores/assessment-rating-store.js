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


    const findSummaryCounts = (summaryRequest, targetKind, force = false) =>
        remote
            .fetchViewList(
                "POST",
                `api/assessment-rating/target-kind/${targetKind}/summary-counts`,
                summaryRequest,
                {force});

    return {
        findByDefinitionId,
        findByEntityKind,
        findSummaryCounts
    };
}


export const assessmentRatingStore = mkAssessmentRatingStore();