import {remote} from "./remote";
import {checkIsEntityRef} from "../common/checks";


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

    const findForEntityReference = (ref, force = false) => remote
        .fetchViewList(
            "GET",
            `api/assessment-rating/entity/${ref.kind}/${ref.id}`,
            null,
            {force});

    const store = (ref, defnId, rating) => remote
        .execute(
            "POST",
            `api/assessment-rating/entity/${ref.kind}/${ref.id}/${defnId}`,
            rating);

    const findRatingPermissions = (ref, defnId, force = false) => remote
        .fetchViewList(
            "GET",
            `api/assessment-rating/entity/${ref.kind}/${ref.id}/${defnId}/permissions`,
            null,
            {force});

    const lock = (ref, defnId, ratingId) => remote
        .execute(
            "PUT",
            `api/assessment-rating/entity/${ref.kind}/${ref.id}/${defnId}/${ratingId}/lock`,
            null);

    const unlock = (ref, defnId, ratingId) => remote
        .execute(
            "PUT",
            `api/assessment-rating/entity/${ref.kind}/${ref.id}/${defnId}/${ratingId}/unlock`,
            null);

    const remove = (ref, defnId, ratingId) => remote
        .execute(
            "DELETE",
            `api/assessment-rating/entity/${ref.kind}/${ref.id}/${defnId}/${ratingId}`);

    const update = (id, comment) => remote
        .execute(
            "POST",
            `api/assessment-rating/id/${id}`,
            {comment});

    return {
        findByDefinitionId,
        findForEntityReference,
        findByEntityKind,
        findRatingPermissions,
        store,
        remove,
        lock,
        unlock,
        update
    };
}


export const assessmentRatingStore = mkAssessmentRatingStore();