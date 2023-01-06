import _ from "lodash";


export function sortItems(items = []) {
    return _.orderBy(
        items,
        ["ratingGroup", "position", "name"])
}

export function countUsageStatsBy(usageData, accessor) {
    return _.reduce(
        usageData,
        (acc, d) => {
            const k = accessor(d)
            acc[k] = (acc[k] || 0) + d.count;
            return acc;
        },
        {});
}