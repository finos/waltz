import {scaleOrdinal} from "d3-scale";
import {hsl} from "d3-color";

export function toChartDimensions(dimensions, margins) {
    return {
        width: dimensions.width - (margins.left + margins.right),
        height: dimensions.height - (margins.top + margins.bottom)
    };
}


export function cmp(a, b) {
    if (a === b) return 0;
    if (a > b) return 1;
    else return -1;
}


export function mkColorScales(data) {
    const categories = _
        .chain(data.nodes)
        .map(d => d.category)
        .uniqBy(d => d.code)
        .value();

    const fgColor = scaleOrdinal()
        .domain(categories.map(d => d.code))
        .range(categories.map(d => d.color));

    const bgColor = scaleOrdinal()
        .domain(fgColor.domain())
        .range(fgColor.range().map(c => hsl(c).brighter(0.2)))

    return {node: fgColor, link: bgColor};
}
