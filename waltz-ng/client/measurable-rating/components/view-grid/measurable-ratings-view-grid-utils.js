import {
    mkAllocationColumns,
    mkDecommissionColumns,
    mkEntityLinkFormatter,
    mkPrimaryAssessmentAndCategoryColumns,
    mkRatingSchemeItemFormatter,
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
    },
    {
        id: "measurable_rating",
        name: "Rating",
        field: "ratingSchemeItem",
        sortable:  true,
        width: 180,
        formatter: mkRatingSchemeItemFormatter(d => d.name, d => d),
        sortFn: (a, b) => cmp(a?.ratingSchemeItem.name, b?.ratingSchemeItem.name)
    }
];


export function doGridSearch(data = [], searchStr) {
    return termSearch(
        data,
        searchStr,
        [
            "application.name",
            "application.assetCode",
            "measurable.name",
            "measurable.externalId",
            "ratingSchemeItem.name",
            "replacementAppsString",
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
        mkSummaryColumn(),
        baseColumns,
        mkAllocationColumns(allocations.allocationSchemes),
        mkPrimaryAssessmentAndCategoryColumns(primaryAssessments.assessmentDefinitions, primaryRatings.measurableCategories),
        mkDecommissionColumns(decommissions.plannedDecommissions, decommissions.plannedReplacements, decommissions.replacingDecommissions));
}


export function mkSummaryFormatter() {
    return (row, cell, value, colDef, dataCtx) => {
        const isPrimary = _.get(dataCtx, ["measurableRating", "isPrimary"], false);
        const hasAllocations = _.get(dataCtx, ["hasAllocations"], false);
        const hasReplacements = !_.isEmpty(_.get(dataCtx, ["replacementApplications"], []));
        const hasPlannedDecom = !_.isEmpty(_.get(dataCtx, ["plannedDecommission"], null));
        const primaryIcon = isPrimary
            ? `<span class="text-muted" style="padding-right: 2px"><i class="small fa fa-fw fa-star-o"></i></span>`
            : `<span class="text-muted" style="padding-right: 2px"><i class="small fa fa-fw"></i></span>`;

        const allocationIcon = hasAllocations
            ? `<span class="text-muted" style="padding-right: 2px"><i class="small fa fa-fw fa-pie-chart"></i></span>`
            : `<span class="text-muted" style="padding-right: 2px"><i class="small fa fa-fw"></i></span>`;

        const decomIcon = hasReplacements
            ? `<span class="text-muted" style="padding-right: 2px"><i class="small fa fa-fw fa-handshake-o"></i></span>`
            : hasPlannedDecom
                ? `<span class="text-muted" style="padding-right: 2px"><i class="small fa fa-fw fa-hand-o-right"></i></span>`
                : `<span class="text-muted" style="padding-right: 2px"><i class="small fa fa-fw fa-fw"></i></span>`;

        return primaryIcon + allocationIcon + decomIcon;
    };
}


export function mkSummaryColumn() {
    return {
        id: "summary_col",
        name: "",
        field: null,
        sortable: false,
        width: 80,
        resizable:true,
        formatter: mkSummaryFormatter()
    };
}


export function mkGridData(applications, measurableRatings, primaryAssessments, primaryRatings, allocationsView, decommsView) {
    const measurablesById = _.keyBy(measurableRatings.measurables, d => d.id);
    const applicationsById = _.keyBy(applications, d => d.id);

    const measurableRatingsSchemeItemsById = _.keyBy(measurableRatings.ratingSchemeItems, d => d.id);
    const primaryRatingsSchemeItemsById = _.keyBy(primaryRatings.ratingSchemeItems, d => d.id);

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

            const ratingSchemeItem = measurableRatingsSchemeItemsById[rating.ratingId];
            const allocationsByRating = allocationsByRatingId[rating.id];
            const plannedDecommission = decommsByRatingId[rating.id];
            const replacementApplications = _.get(replacementsByDecommId, plannedDecommission?.id, []);

            const replacementAppsString = _
                .chain(replacementApplications)
                .map(d => d.entityReference.name)
                .join(" ")
                .value();

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
                    const ratingSchemeItem = primaryRatingsSchemeItemsById[mr.ratingId];
                    return {
                        measurable,
                        measurableCategory,
                        ratingSchemeItem
                    };
                })
                .reduce((acc, d) => {acc['measurable_category/'+d.measurableCategory.id] = d; return acc;}, {})
                .value();

            return _.merge(
                {
                    measurable,
                    application,
                    measurableRating: rating,
                    ratingSchemeItem,
                    plannedDecommission,
                    replacementApplications,
                    hasAllocations: !_.isEmpty(allocationsByRating),
                    replacementAppsString
                },
                primaryAssessmentCells,
                primaryMeasurableCells,
                allocationCells);
        })
        .value();

}