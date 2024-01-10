import {
    mkApplicationKindFormatter, mkAssessmentAndCategoryColumns,
    mkEntityLinkFormatter,
    mkLifecyclePhaseFormatter
} from "../../../common/slick-grid-utils";
import {cmp} from "../../../common/sort-utils";
import {termSearch} from "../../../common";
import _ from "lodash";


export const baseColumns = [
    {
        id: "name",
        name: "Name",
        field: "application",
        sortable:  true,
        width: 180,
        formatter: mkEntityLinkFormatter(null, false),
        sortFn: (a, b) => cmp(a?.application.name, b?.application.name)
    }, {
        id: "assetCode",
        name: "Asset Code",
        field: "application_assetCode",
        sortable:  true
    }, {
        id: "kind",
        name: "Kind",
        field: "application_applicationKind",
        sortable:  true,
        formatter: mkApplicationKindFormatter()
    }, {
        id: "orgUnit",
        name: "Org Unit",
        field: "organisationalUnit",
        sortable: true,
        width: 180,
        formatter: mkEntityLinkFormatter(null, false),
        sortFn: (a, b) => cmp(a?.organisationalUnit.name, b?.organisationalUnit.name)
    }, {
        id: "lifecyclePhase",
        name: "Lifecycle Phase",
        field: "application_lifecyclePhase",
        width: 90,
        sortable:  true,
        formatter: mkLifecyclePhaseFormatter()
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


export function mkColumnDefs(primaryAssessments, primaryRatings) {
    return _.concat(
        baseColumns,
        mkAssessmentAndCategoryColumns(
            primaryAssessments.assessmentDefinitions,
            primaryRatings.measurableCategories));
}


export function mkGridData(applications, primaryAssessments, primaryRatings, orgUnitsById) {
    const measurableRatingsByAppId = _.groupBy(primaryRatings.measurableRatings, d => d.entityReference.id);
    const measurablesById = _.keyBy(primaryRatings.measurables, d => d.id);
    const measurableCategoriesById = _.keyBy(primaryRatings.measurableCategories, d => d.id);
    const measurableRatingsSchemeItemsByCategoryThenCode = _
        .chain(primaryRatings.measurableCategories)
        .reduce(
            (acc, mc) => {
                acc[mc.id] = _.keyBy(
                    _.filter(primaryRatings.ratingSchemeItems, rsi => rsi.ratingSchemeId === mc.ratingSchemeId),
                    rsi => rsi.rating);
                return acc;
            },
            {})
            .value();
    const assessmentRatingsByAppId = _.groupBy(primaryAssessments.assessmentRatings, d => d.entityReference.id);
    const assessmentDefinitionsById = _.keyBy(primaryAssessments.assessmentDefinitions, d => d.id);
    const assessmentRatingsSchemeItemsById = _.keyBy(primaryAssessments.ratingSchemeItems, d => d.id);

    return _
        .chain(applications)
        .map(app => {
            const base = {
                application: app,
                application_assetCode: app.assetCode,
                application_applicationKind: app.applicationKind,
                application_lifecyclePhase: app.lifecyclePhase,
                organisationalUnit: orgUnitsById[app.organisationalUnitId],
            };
            const measurableRatings = measurableRatingsByAppId[app.id] || [];
            const assessmentRatings = assessmentRatingsByAppId[app.id] || [];
            const primaryMeasurableCells = _
                .chain(measurableRatings)
                .map(mr => {
                    const measurable = measurablesById[mr.measurableId];
                    const measurableCategory = measurableCategoriesById[measurable.categoryId];
                    const ratingSchemeItem = _.get(measurableRatingsSchemeItemsByCategoryThenCode, [measurableCategory.id, mr.rating]);
                    return {
                        measurable,
                        measurableCategory,
                        ratingSchemeItem
                    };
                })
                .reduce((acc, d) => {acc['measurable_category/'+d.measurableCategory.externalId] = d; return acc;}, {})
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
                        const key = 'assessment_definition/'+d.assessmentDefinition.externalId;
                        const values = acc[key] || [];
                        values.push(d);
                        acc[key] = values;
                        return acc;
                    },
                    {}
                )
                .value();

             return _.merge(base, primaryMeasurableCells, primaryAssessmentCells);
        })
        .value();

}