import _ from "lodash";


export function sortItems(items = []) {
    return _.orderBy(
        items,
        ["position", "name"])
}