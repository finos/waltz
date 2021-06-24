import {blue, red} from "../../../common/colors"
import {
    symbol,
    symbolCircle,
    symbolCross,
    symbolDiamond,
    symbolSquare,
    symbolStar,
    symbolTriangle,
    symbolWye
} from "d3-shape";

export function determineStylingBasedUponLifecycle(status) {
    switch (status) {
        case "PENDING":
            return {color: blue, dashArray: "6 3"};
        case "REMOVED":
            return {color: red, dashArray: "3 6"};
        default:
            return {color: "#888888", dashArray: "0"};
    }
}


export const symbolsByName = {
    "triangle": symbol().type(symbolTriangle).size(40),
    "circle": symbol().type(symbolCircle).size(60),
    "diamond": symbol().type(symbolDiamond).size(30),
    "cross": symbol().type(symbolCross).size(40),
    "star": symbol().type(symbolStar).size(30),
    "wye": symbol().type(symbolWye).size(50),
    "square": symbol().type(symbolSquare).size(60),
    "DEFAULT": symbol().type(symbolWye).size(40)
};
