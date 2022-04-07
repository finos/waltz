import _ from "lodash";

export function prepareMonthData(data = [], startDate, endDate) {

    let months = [];

    const startMonth = startDate.getMonth();
    const startYear = startDate.getFullYear();

    let initialCalendarDate = new Date(startYear, startMonth, 1);

    while (initialCalendarDate < endDate) {
        const date = new Date(initialCalendarDate);

        const month = {
            startDate: date,
            days: mkDaysForMonth(data, date)
        }

        months.push(month);
        initialCalendarDate = new Date(initialCalendarDate.setMonth(initialCalendarDate.getMonth() + 1))
    }

    return months
}


function mkDateKeyFromDateStr(dateStr) {
    const date = new Date(dateStr);
    return mkDateKeyFromComponents(date.getMonth() + 1, date.getDate(), date.getFullYear());
}


function mkDateKeyFromComponents(month, day, year) {
    return year * 10000 + month * 100 + day;
}


function toDateFromDateKey(dateKey) {
    let year = Math.floor(dateKey / 10000);
    let month = Math.floor(dateKey % 10000 / 100);
    let date = Math.floor(dateKey % 100);

    return new Date(year, month - 1, date);
}

function mkDaysForMonth(data, date) {

    let month = date.getMonth() + 1;
    let year = date.getFullYear();
    let dayCount = daysInMonth(month, year);

    let dataByDate = _.keyBy(data, d => mkDateKeyFromDateStr(d.date));

    return _.map(_.range(dayCount), x => {

        let day = x + 1;

        let dateKey = mkDateKeyFromComponents(month, day, year);

        let value = _.get(dataByDate, [dateKey, "count"], 0);

        return {date: toDateFromDateKey(dateKey), value}
    });
}


export function daysInMonth(month, year) {
    return new Date(year, month, 0).getDate();
}


export const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

export const dimensions = {
    diagram: {
        width: 2400,
        height: 800
    },
    day: {
        width: 20
    },
    month: {
        width: 150,
        height: 160
    },
    circleRadius: 8,
    weekPadding: 10,
    monthsPerLine: 6
}