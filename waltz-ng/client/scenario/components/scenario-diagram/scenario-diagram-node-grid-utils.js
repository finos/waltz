import _ from "lodash";
import {drawUnit, NODE_STYLES, updateUnit} from "./scenario-diagram-static-node-utils";
import {checkTrue} from "../../../common/checks";
import {CELL_DIMENSIONS} from "./scenario-diagram-dimensions";
import {defaultOptions} from "./scenario-diagram-utils";
import {d3ContextMenu} from "../../../common/d3-context-menu";

const EMPTY_CELL_WIDTH = 0.5;
const EMPTY_CELL_HEIGHT = 0.5;


export function drawNodeGrid(selection, options) {
    const dataProvider = d => d.data;

    const cells = selection
        .selectAll(`g.${NODE_STYLES.nodeCell}`)
        .data(
            dataProvider,
            d => d.id);

    const contextMenu = _.get(options, ["handlers", "contextMenus", "node"], null);

    const newCells = cells
        .enter()
        .append("g")
        .classed(NODE_STYLES.nodeCell, true)
        .call(drawUnit, options)
        .on("contextmenu", contextMenu ? d3ContextMenu(contextMenu) : null);

    cells
        .exit()
        .remove();

    newCells
        .merge(cells)
        .attr("transform", d => {
            const dy = CELL_DIMENSIONS.padding + (CELL_DIMENSIONS.height * d.layout.row);
            const dx = CELL_DIMENSIONS.padding + (CELL_DIMENSIONS.width * d.layout.col);
            return `translate(${dx} ${dy})`;
        })
        .call(updateUnit, options);
}


/**
 * Given an _array_ of data will return an _object_ similar to:
 *
 * ```
 * {
 *     data: [ {
 *         ...datum,   // original data
 *         layout: { col: x, row: y }  // position within grid (zero offset)
 *     }],
 *     layout: {
 *       colCount: x,  // max number of cols (<= options.cols)
 *       rowCount: y   // max number rows
 *     }
 * }
 * ```
 *
 * @param data
 * @param coords - `{row: n, col: n}` used to generate unique id
 * @param options
 * @returns {{data: Array, layout: {colCount: number, rowCount: number}}}
 **/
export function nodeGridLayout(data = [], coords, options = defaultOptions) {
    checkTrue(options.defaultColMaxWidth > 0, "gridLayout: Num cols must be greater than zero");
    // head is safe because the whole data array deals with the same domain coordinates
    const columnDomainId = _.get(_.head(data), ["domainCoordinates", "column", "id"]);
    const maxColWidth = options.maxColWidths[columnDomainId] || options.defaultColMaxWidth;

    const dataWithLayout = _
        .chain(data)
        .orderBy(options.sortFn)
        .map((d, idx) => {
            const layout = {
                col: idx % maxColWidth,
                row: Math.floor(idx / maxColWidth)
            };
            return Object.assign({}, d, { layout });
        })
        .value();

    const layout = {
        colCount: Math.min(maxColWidth, data.length) || EMPTY_CELL_WIDTH,
        rowCount: Math.ceil(data.length / maxColWidth) || EMPTY_CELL_HEIGHT
    };

    return {
        id: `${coords.col},${coords.row}`,
        kind: "nodeGridLayout",
        data: dataWithLayout,
        layout
    };
}



