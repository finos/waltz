import {toCumulativeCounts} from "../../../common/list-utils";

export function calcHeight(numRows, dimensions) {
    return numRows * dimensions.cell.height;
}



export function layout(groups = [], dimensions) {

    const totalRows = _.sumBy(
        groups,
        d => d.rows.length);

    dimensions.height = calcHeight(totalRows, dimensions);

    const cumulativeRowCounts = toCumulativeCounts(_.map(groups, g => g.rows.length));


    return _.map(groups, (g, idx) => {
        const precedingRows = cumulativeRowCounts[idx] - g.rows.length;
        const layoutData = {
            precedingRows: precedingRows,
            rowsInGroup: g.rows.length,
            dy: calcHeight(precedingRows, dimensions),
            height: calcHeight(g.rows.length, dimensions)
        };
        return Object.assign({}, g, {layoutData});
    });
}