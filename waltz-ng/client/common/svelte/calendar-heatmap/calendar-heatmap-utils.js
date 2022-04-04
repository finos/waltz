import {toLocalDate} from "../../date-utils";

export function chunkMonths(data = [], startDate, endDate) {

    console.log({data});
    let stDate = toLocalDate(startDate);
    let edDate = toLocalDate(endDate);

    console.log(stDate);

    return [{monthStartDate: startDate, days: [{date: endDate}]}]
    _.chain(data)

}