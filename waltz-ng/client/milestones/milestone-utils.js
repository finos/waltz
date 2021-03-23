import _ from "lodash";
import {extent} from "d3-array";
import {timeFormat} from "d3-time-format";

function removeFromAcc(id, acc) {

    _.each(acc, (v, k) => {
        acc[k] = _.without(v, id);
    });

    return acc;
}


function bucketItemByRating(acc, d) {
    const accNext = removeFromAcc(d.id_a, acc);
    if (d.rating_id){
        accNext[d.rating_id] = _.concat(
            (accNext[d.rating_id] || []),
            [d.id_a]);
    }
    return Object.assign({}, accNext);
}


export function calcDateExtent(rawData = [], endPadInDays = 0) {
    const dateRange = extent(_
        .chain(rawData)
        .map(d => d.milestone_date)
        .map(Date.parse)
        .value());

    const paddedDateRange = [
        dateRange[0],
        dateRange[1] + (endPadInDays * 1000 * 60 * 60 * 24)
    ];

    return paddedDateRange;

}

export function toStackData(data) {
    const groupedByDate = _
        .chain(data)
        .groupBy("milestone_date")
        .map((v, k) => ({k: Date.parse(k), v}))
        .orderBy(d => d.k)
        .value();

    const dates = _.map(groupedByDate, d => d.k);
    const durations = _.zip(dates, _.tail(dates));

    let xs = [];
    let acc = {};
    _.each(
        groupedByDate,
        (d, i) => {
            acc = _.reduce(
                d.v,
                bucketItemByRating,
                acc);
            xs.push({
                k: d.k,
                s: durations[i][0],
                e: durations[i][1],
                values: acc});
        });

    return xs;
}



const dateFormat = timeFormat("%B %d, %Y");

export function prettyDate(d) {
    return dateFormat(d);
}


function findStratum(stackData, t) {
    return _.find(
        stackData,
        d => d.s < t && (d.e > t || _.isUndefined(d.e)));
}


export function findStrata(xs, t) {
    return _
        .chain(xs)
        .map(d => {
            return {
                k: d.k,
                stratum: findStratum(d.stackData, t)
            }
        })
        .orderBy(d => d.k)
        .value();
}
