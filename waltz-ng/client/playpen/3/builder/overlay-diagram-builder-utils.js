import {toCumulativeCounts} from "../../../common/list-utils";

export function calcHeight(numRows, dimensions) {
    return numRows * dimensions.cell.height;
}



export function layout(groups = [], dimensions) {

    const groupHeights = _.chain(groups)
        .map(g => _.get(g, ["cellHeight"], dimensions.cell.height) * g.rows.length)
        .value();

    let cumulativeHeights = toCumulativeCounts(groupHeights);

    dimensions.height = _.max(_.values(cumulativeHeights));
    const cumulativeRowCounts = toCumulativeCounts(_.map(groups, g => g.rows.length));

    return _.map(groups, (g, idx) => {
        const precedingRows = cumulativeRowCounts[idx] - g.rows.length;
        const layoutData = {
            precedingRows: precedingRows,
            rowsInGroup: g.rows.length,
            dy: cumulativeHeights[idx] - groupHeights[idx],
            height: groupHeights[idx]
        };
        return Object.assign({}, g, {layoutData});
    });
}