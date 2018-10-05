import {NODE_DIMENSIONS} from "./scenario-diagram-static-node-utils";

const CELL_PADDING = 10;


export const CELL_DIMENSIONS = {
    padding: CELL_PADDING,
    height: NODE_DIMENSIONS.height + CELL_PADDING,
    width: NODE_DIMENSIONS.width + CELL_PADDING
};


export const ROW_CELL_DIMENSIONS = {
    padding: CELL_PADDING * 2
};


export const ROW_DIMENSIONS = {
    padding: CELL_PADDING * 2
};

