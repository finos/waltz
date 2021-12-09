import {timeFormat} from "d3-time-format";

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
    return dateFormat(inputDate);
}