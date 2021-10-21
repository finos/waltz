import {remote} from "./remote";


export function mkAssessmentRatingViewStore() {

    const findEntitiesGroupedByDefinitionAndOutcome = (entityKind, entityIds = [], force = false) => remote
        .fetchViewList(
            "POST",
            `api/assessment-rating-view/kind/${entityKind}/grouped`,
            entityIds,
            [],
            {force});

    const findFavouritesForEntity = (ref, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `api/assessment-rating-view/kind/${ref.kind}/id/${ref.id}`,
                [],
                {force});
    }

    return {
        findEntitiesGroupedByDefinitionAndOutcome,
        findFavouritesForEntity
    };
}


export const assessmentRatingViewStore = mkAssessmentRatingViewStore();