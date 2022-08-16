import {timeFormat} from "d3-time-format";
import _ from "lodash";

/**
 * Mutates a date so it is in line with UTC (but does not change it's timezone.
 * Useful for date pickers as it retains the date selection regardless of
 * differences between the server and the client.
 *
 * @param inputDate
 * @returns {*}
 */
export function alignDateToUTC(inputDate) {
    inputDate.setMinutes(inputDate.getMinutes() - inputDate.getTimezoneOffset());
    return inputDate;
}


const dateFormat = timeFormat("%Y-%m-%d");

export function toLocalDate(inputDate) {
    return dateFormat(_.isString(inputDate)
        ? new Date(inputDate)
        : inputDate);
}

/**
 * determines if `d2` is within `monthDelta` of `d2`
 * @param d1
 * @param d2
 * @param monthDelta
 * @returns {boolean}
 */
export function withinMonths(d1, d2, monthDelta) {
    return d1.setMonth(d1.getMonth() - monthDelta) <= d2;
}


export function subtractYears(numYears, date = new Date()) {
    date.setFullYear(date.getFullYear() - numYears);
    return date;
}