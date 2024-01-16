import _ from "lodash";

export function cmp(a, b) {
    return a > b
        ? 1
        : a < b
            ? -1
            : 0;
}

export function refCmp(a, b) {
    return cmp(a.name, b.name);
}

export function compareDates(date1, date2) {
    if(!_.isEmpty(date1) && _.isEmpty(date2)){
        return 1;
    } else if (_.isEmpty(date1) && !_.isEmpty(date2)){
        return -1;
    } else {
        const d1 = Date.parse(date1);
        const d2 = Date.parse(date2);
        return d1 > d2
            ? 1
            : d1 < d2
                ? -1
                : 0;
    }
}