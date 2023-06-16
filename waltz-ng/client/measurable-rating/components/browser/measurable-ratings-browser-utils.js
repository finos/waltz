import _ from "lodash";
import {buildHierarchies} from "../../../common/hierarchy-utils";


function combineRatingTallies(r1, r2) {
    return _.mergeWith(
        {}, r1, r2,
        (v1, v2) => (v1 || 0) + (v2 || 0));
}


function toRatingsSummaryObj(ratings = []) {

    const counts = _.chain(ratings)
        .groupBy(r => r.rating)
        .mapValues(r => {

            const countsForRating = _.map(_.values(r), r => r.count);
            return _.sum(countsForRating);
        })
        .value();

    const total = _.sum(_.values(counts));
    return Object.assign({}, counts, { total });
}


function initialiseRatingTalliesMap(ratingTallies = [], measurables = []) {
    const talliesById = _.groupBy(ratingTallies, "id");

    const reducer = (acc, m) => {
        const talliesForMeasurable = talliesById[m.id];
        const summaryObj = talliesForMeasurable
            ? toRatingsSummaryObj(talliesForMeasurable)
            : {};

        acc[m.id] = {
            direct: _.clone(summaryObj),
            compound: _.clone(summaryObj),
        };
        return acc;
    };
    return _.reduce(measurables, reducer, {});
}


export function prepareTreeData(data = []) {
    return buildHierarchies(data, false);
}


export function prepareTabs(categories = [], measurables = [], ratingSchemesById = {}) {
    const measurablesByCategory = _.groupBy(measurables, "categoryId");
    return _
        .chain(categories)
        .filter(category => _.get(measurablesByCategory, category.id, []).length > 0)
        .map(category => {
            const measurablesForCategory = measurablesByCategory[category.id];
            const treeData = prepareTreeData(measurablesForCategory);
            const maxSize = _.chain(treeData)
                .map("totalRatings.total")
                .max()
                .value();

            return {
                category,
                ratingScheme: ratingSchemesById[category.ratingSchemeId],
                treeData,
                maxSize,
                expandedNodes: []
            };
        })
        .sortBy(d => d.category.position, d => d.category.name)
        .value();
}


export function findFirstNonEmptyTab(tabs = []) {
    const firstNonEmptyTab = _.find(tabs, t => t.treeData.length > 0);
    return firstNonEmptyTab || tabs[0];
}


export function mkRatingTalliesMap(ratingTallies = [], measurables = []) {
    const measurablesById = _.keyBy(measurables, "id");
    const talliesMap = initialiseRatingTalliesMap(ratingTallies, measurables);
    _.each(measurables, m => {
        const rs = talliesMap[m.id];

        while (m.parentId) {
            const parent = measurablesById[m.parentId];
            if (! parent) break;
            const parentRating = talliesMap[m.parentId];
            parentRating.compound = combineRatingTallies(parentRating.compound, rs.direct);
            m = parent;
        }
    });

    return talliesMap;
}


export function findMaxTotal(ratingsMap) {
    return _.max(
        _.flatMap(
            _.values(ratingsMap),
            r => _.get(r, ["compound", "total"], [0])))
}