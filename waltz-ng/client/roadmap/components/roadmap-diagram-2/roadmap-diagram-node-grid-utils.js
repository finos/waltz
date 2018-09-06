import {drawUnit, NODE_DIMENSIONS, NODE_STYLES} from "./roadmap-diagram-node-utils";
import {checkTrue} from "../../../common/checks";


const CELL_PADDING = 10;
const CELL_HEIGHT = NODE_DIMENSIONS.height + CELL_PADDING;
const CELL_WIDTH = NODE_DIMENSIONS.width + CELL_PADDING;


export function drawNodeGrid(selection, gridData, ratingColorScheme) {
    const grid = selection
        .selectAll(`.${NODE_STYLES.node}`)
        .data(gridData.data, d => d.id);

    const newCells = grid
        .enter()
        .append("g")
        .classed(NODE_STYLES.node, true);

    grid.exit()
        .remove();

    grid.merge(newCells)
        .attr("transform", d => {
            const dy = CELL_PADDING + (CELL_HEIGHT * d.layout.row);
            const dx = CELL_PADDING + (CELL_WIDTH * d.layout.col);
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

    const dimensions = {
        cols: options.cols,
        rows: Math.ceil(data.length / options.cols)
    };

    return {
        data: dataWithLayout,
        dimensions
    };
}



