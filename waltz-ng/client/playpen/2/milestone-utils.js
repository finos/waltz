import _ from "lodash";
import {extent} from "d3-array";

function removeFromAcc(id, acc) {
    return {
        a: _.without(acc.a, id),
        r: _.without(acc.r, id),
        g: _.without(acc.g, id)
    };
}


function categorizeBucket(acc, d) {
    const accNext = removeFromAcc(d.id_a, acc);
    if (d.milestone_name === 'Launch Date') {
        accNext.g.push(d.id_a);
        return accNext;
    }
    if (d.milestone_name === 'Hold Date') {
        accNext.a.push(d.id_a);
        return accNext;
    }
    if (d.milestone_name === 'Sell Date') {
        accNext.r.push(d.id_a);
        return accNext;
    }
    return accNext;
}


export function calcDateExtent(rawData = [], endPadInDays = 0) {
    const dateRange = extent(_
        .chain(rawData)
        .map(d => d.milestone_date)
        .map(Date.parse)
        .value());

    const paddedDateRange = [dateRange[0], dateRange[1] + (endPadInDays * 1000 * 60 * 60 * 24)];
    console.log({dateRange, paddedDateRange});

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
    let acc = {r: [], a: [], g: []};
    _.each(
        groupedByDate,
        (d, i) => {
            acc = _.reduce(d.v, categorizeBucket, acc);
            xs.push({k: d.k, s: durations[i][0], e: durations[i][1], values: acc});
        });

    console.log("lol", {xs});

    return xs;
}
