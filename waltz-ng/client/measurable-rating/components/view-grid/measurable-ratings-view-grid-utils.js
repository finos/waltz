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
    const applicationsById = _.keyBy(applications, d => d.id);
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
    const assessmentRatingsByEntityId = _.groupBy(primaryAssessments.assessmentRatings, d => d.entityReference.id);
    const assessmentDefinitionsById = _.keyBy(primaryAssessments.assessmentDefinitions, d => d.id);
    const assessmentRatingsSchemeItemsById = _.keyBy(primaryAssessments.ratingSchemeItems, d => d.id);

    return _
        .chain(primaryRatings.measurableRatings)
        .map(rating => {
            const assessmentRatings = assessmentRatingsByEntityId[rating.id] || [];
            const measurable = measurablesById[rating.measurableId];
            const application = applicationsById[rating.entityReference.id];

            const ratingSchemeItemsByCode = _.get(measurableRatingsSchemeItemsByCategoryThenCode, [measurable?.measurableCategoryId], {});
            const ratingSchemeItem = ratingSchemeItemsByCode[rating.rating];

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

             return _.merge({measurable, application, ratingSchemeItem}, primaryAssessmentCells);
        })
        .value();

}