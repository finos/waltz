import {remote} from "./remote";


export function mkMeasurableRelationshipStore() {

    const getById = (id, force  = false) => remote
        .fetchViewData(
            "GET",
            `api/measurable-rating/id/${id}`,
            null,
            null,
            {force})

    const getViewById = (id, force  = false) => remote
        .fetchViewData(
            "GET",
            `api/measurable-rating/id/${id}/view`,
            null,
            null,
            {force})

    const findByApplicationSelector = (options) => remote
        .execute(
            "POST",
            "api/measurable-rating/app-selector",
            options);

    const getViewByCategoryAndSelector = (categoryId, options, force = false) => remote
        .fetchViewData(
            "POST",
            `api/measurable-rating/category/${categoryId}/view`,
            options,
            null,
            {force});

    const getPrimaryRatingsViewBySelector = (options, force = false) => remote
        .fetchViewData(
            "POST",
            "api/measurable-rating/primary-ratings/view",
            options,
            null,
            {force});

    const bulkRatingPreviewByCategory = (categoryId, uploadMode, data) => remote
        .execute(
            "POST",
            `api/measurable-rating/bulk/preview/MEASURABLE_CATEGORY/${categoryId}?mode=${uploadMode}`,
            data);

    const bulkRatingApplyByCategory = (categoryId, uploadMode, data) => remote
        .execute(
            "POST",
            `api/measurable-rating/bulk/apply/MEASURABLE_CATEGORY/${categoryId}?mode=${uploadMode}`,
            data);

    return {
        findByApplicationSelector,
        getById,
        getViewById,
        getViewByCategoryAndSelector,
        getPrimaryRatingsViewBySelector,
        bulkRatingPreviewByCategory,
        bulkRatingApplyByCategory
    };
}



export const measurableRatingStore = mkMeasurableRelationshipStore();