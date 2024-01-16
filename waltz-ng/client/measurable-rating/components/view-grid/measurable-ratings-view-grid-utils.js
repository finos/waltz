import {
    mkAllocationColumns,
    mkApplicationKindFormatter, mkPrimaryAssessmentAndCategoryColumns, mkDecommissionColumns,
    mkEntityLinkFormatter,
    mkLifecyclePhaseFormatter
} from "../../../common/slick-grid-utils";
import {cmp} from "../../../common/sort-utils";
import {termSearch} from "../../../common";
import _ from "lodash";


export const baseColumns = [
    {
        id: "application_name",
        name: "Application",
        field: "application",
        sortable:  true,
        width: 180,
        formatter: mkEntityLinkFormatter(null, false),
        sortFn: (a, b) => cmp(a?.application.name, b?.application.name)
    },
    {
        id: "measurable_name",
        name: "Taxonomy Item",
        field: "measurable",
        sortable:  true,
        width: 180,
        formatter: mkEntityLinkFormatter(null, false),
        sortFn: (a, b) => cmp(a?.measurable.name, b?.measurable.name)
    }
];


export function doGridSearch(data = [], searchStr) {
    return termSearch(
        data,
        searchStr,
        [
            "application.name",
            "application.assetCode",
            "application.lifecyclePhase",
            "application.applicationKind",
            "organisationalUnit.name",
            d => _
                .chain(_.keys(d))
                .filter(k => k.startsWith("measurable_category"))
                .map(k => d[k].measurable.name)
                .join(" ")
                .value(),
            d => _
                .chain(_.keys(d))
                .filter(k => k.startsWith("assessment_definition"))
                .flatMap(k => d[k])
                .map(r => r.ratingSchemeItem.name)
                .join(" ")
                .value()
        ]);
}


export function mkColumnDefs(measurableRatings, primaryAssessments, primaryRatings, allocations, decommissions) {
    return _.concat(
        baseColumns,
        mkPrimaryAssessmentAndCategoryColumns(primaryAssessments.assessmentDefinitions, primaryRatings.measurableCategories),
        mkAllocationColumns(allocations.allocationSchemes),
        mkDecommissionColumns(decommissions.plannedDecommissions, decommissions.plannedReplacements, decommissions.replacingDecommissions));
}


export function mkGridData(applications, measurableRatings, primaryAssessments, primaryRatings, allocationsView, decommsView) {
    const measurablesById = _.keyBy(measurableRatings.measurables, d => d.id);
    const applicationsById = _.keyBy(applications, d => d.id);
    const measurableRatingsSchemeItemsByCategoryThenCode = _
        .chain(measurableRatings.measurableCategories)
        .reduce(
            (acc, mc) => {
                acc[mc.id] = _.keyBy(
                    _.filter(measurableRatings.ratingSchemeItems, rsi => rsi.ratingSchemeId === mc.ratingSchemeId),
                    rsi => rsi.rating);
                return acc;
            },
            {})
            .value();
    const primaryRatingsSchemeItemsByCategoryThenCode = _
        .chain(primaryRatings.measurableCategories)
        .reduce(
            (acc, mc) => {
                acc[mc.id] = _.keyBy(
                    _.filter(measurableRatings.ratingSchemeItems, rsi => rsi.ratingSchemeId === mc.ratingSchemeId),
                    rsi => rsi.rating);
                return acc;
            },
            {})
            .value();

    const assessmentRatingsByEntityId = _.groupBy(primaryAssessments.assessmentRatings, d => d.entityReference.id);
    const assessmentDefinitionsById = _.keyBy(primaryAssessments.assessmentDefinitions, d => d.id);
    const assessmentRatingsSchemeItemsById = _.keyBy(primaryAssessments.ratingSchemeItems, d => d.id);

    const allocationsByRatingId = _.groupBy(allocationsView.allocations, d => d.measurableRatingId);

    const decommsByRatingId = _.keyBy(decommsView.plannedDecommissions, d => d.measurableRatingId);
    const replacementsByDecommId = _.groupBy(decommsView.plannedReplacements, d => d.decommissionId);

    const primaryRatingsByEntityId = _.groupBy(primaryRatings.measurableRatings, d => d.entityReference.id);
    const primaryCategoriesById = _.keyBy(primaryRatings.measurableCategories, d => d.id);
    const primaryMeasurablesById =_.keyBy(primaryRatings.measurables, d => d.id);

    return _
        .chain(measurableRatings.measurableRatings)
        .map(rating => {
            const assessmentRatings = assessmentRatingsByEntityId[rating.id] || [];
            const primaryRatings = primaryRatingsByEntityId[rating.entityReference.id] || [];
            const measurable = measurablesById[rating.measurableId];
            const application = applicationsById[rating.entityReference.id];

            const ratingSchemeItemsByCode = _.get(measurableRatingsSchemeItemsByCategoryThenCode, [measurable?.measurableCategoryId], {});
            const ratingSchemeItem = ratingSchemeItemsByCode[rating.rating];
            const allocationsByRating = allocationsByRatingId[rating.id];
            const plannedDecommission = decommsByRatingId[rating.id];
            const replacementApplications = _.get(replacementsByDecommId, plannedDecommission?.id, []);

            const allocationCells = _
                .chain(allocationsByRating)
                .reduce((acc, d) => {acc['allocation_scheme/'+d.schemeId] = d; return acc;}, {})
                .value();

            const primaryAssessmentCells = _
                .chain(assessmentRatings)
                .map(ar => {
                    const assessmentDefinition = assessmentDefinitionsById[ar.assessmentDefinitionId];
                    const ratingSchemeItem = assessmentRatingsSchemeItemsById[ar.ratingId];
                    return {
                        assessmentDefinition,
                        ratingSchemeItem,
                        assessmentRating: ar
                    };
                })
                .reduce(
                    (acc, d) => {
                        const key = 'assessment_definition/'+d.assessmentDefinition.id;
                        const values = acc[key] || [];
                        values.push(d);
                        acc[key] = values;
                        return acc;
                    },
                    {}
                )
                .value();

            const primaryMeasurableCells = _
                .chain(primaryRatings)
                .map(mr => {
                    const measurable = primaryMeasurablesById[mr.measurableId];
                    const measurableCategory = primaryCategoriesById[measurable.categoryId];
                    const ratingSchemeItem = _.get(primaryRatingsSchemeItemsByCategoryThenCode, [measurableCategory.id, mr.rating]);
                    return {
                        measurable,
                        measurableCategory,
                        ratingSchemeItem
                    };
                })
                .reduce((acc, d) => {acc['measurable_category/'+d.measurableCategory.id] = d; return acc;}, {})
                .value();

            return _.merge({
                 measurable,
                 application,
                 ratingSchemeItem,
                 plannedDecommission,
                 replacementApplications},
                 primaryAssessmentCells,
                 primaryMeasurableCells,
                 allocationCells);
        })
        .value();

}