const CELL_PADDING = 10;


export const NODE_DIMENSIONS = {
    width: 120,
    height: 54,  // 3 * section.height
    section: {
        height: 18
    },
    text: {
        dy: 12,
        dx: 4,
        fontSize: 12
    }
};


export const CELL_DIMENSIONS = {
    padding: CELL_PADDING,
    height: NODE_DIMENSIONS.height + CELL_PADDING,
    width: NODE_DIMENSIONS.width + CELL_PADDING
};

