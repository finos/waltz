import {drawUnit, NODE_STYLES} from "./roadmap-diagram-node-utils";
import {checkTrue} from "../../../common/checks";
import {CELL_DIMENSIONS, NODE_DIMENSIONS} from "./roadmap-diagram-dimensions";

export function drawNodeGrid(selection, ratingColorScheme) {
    const grid = selection
        .selectAll(`.${NODE_STYLES.node}`)
        .data(d => d.data, d => d.id);

    const newCells = grid
        .enter()
        .append("g")
        .classed(NODE_STYLES.node, true);

    grid.exit()
        .remove();

    grid.merge(newCells)
        .attr("transform", d => {
            const dy = CELL_DIMENSIONS.padding + (CELL_DIMENSIONS.height * d.layout.row);
            const dx = CELL_DIMENSIONS.padding + (CELL_DIMENSIONS.width * d.layout.col);
            return `translate(${dx} ${dy})`;
        })
        .call(drawUnit, ratingColorScheme);
}


export function gridLayout(data = [], options = { cols: 3 }) {
    checkTrue(options.cols > 0, "gridLayout: Num cols must be greater than zero");

    const dataWithLayout = _.map(
        data,
        (d, idx) => {
            const layout = {
                col: idx % options.cols,
                row: Math.floor(idx / options.cols)
            };
            return Object.assign({}, d, { layout });
        });

    const layout = {
        colCount: Math.min(options.cols, data.length),
        rowCount: Math.ceil(data.length / options.cols)
    };

    return {
        data: dataWithLayout,
        layout
    };
}



