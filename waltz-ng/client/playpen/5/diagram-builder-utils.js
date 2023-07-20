import {
    amberBgHex,
    amberHex, blueBgHex,
    blueHex, determineForegroundColor,
    goldHex, greenBgHex,
    greenHex, greyBgHex,
    greyHex, pinkBgHex,
    pinkHex, purpleBgHex,
    purpleHex, redBgHex,
    redHex, yellowBgHex,
    yellowHex
} from "../../common/colors";
import _ from "lodash";

export const FlexDirections = {
    COLUMN: "column",
    ROW: "row"
}

export const DefaultProps = {
    minHeight: 5,
    minWidth: 10,
    flexDirection: FlexDirections.ROW,
    showTitle: true,
    showBorder: true,
    bucketSize: 3,
    proportion: 1,
    titleColor: "#000d79",
    contentColor: "#f1f1ff",
    contentFontSize: 0.7,
    titleFontSize: 0.8
}

export function mkGroup(title = "Unknown", id, parentId = null, position = 0, properties = DefaultProps, data = null) {

    const props = Object.assign({}, DefaultProps, properties);

    return {
        title,
        id,
        parentId,
        props,
        position,
        data
    }
}

export function mkColourProps(color) {
    return `
    background-color: ${color};
    color: ${determineForegroundColor(color)};`
}

export function mkGroupStyle(group, child) {
    return `
        flex: ${child.props.proportion} 1 ${_.floor(100 / (group.props.bucketSize + 1))}%;
        margin: 0.2em;
        min-width: ${group.props.minWidth}em;
        min-height: ${group.props.minHeight}em;
        height: fit-content;
        width: fit-content;
        ${group.props.flexDirection === FlexDirections.ROW ? "height: fit-content;" : "width: min-content;"}`;
}

export function mkItemStyle(group) {
    return `
        margin: 0.1em;
        padding: 0.1em;
        min-width: ${group.props.minWidth}em;
        min-height: ${group.props.minHeight}em;
        height: fit-content;
        width: fit-content;
        ${group.props.flexDirection === FlexDirections.ROW ? "height: fit-content;" : "width: fit-content;"}
        font-size: ${group.props.contentFontSize}em;
        `;
}

export function mkReorderBoxStyle(group) {
    return `
        margin: 0 0.2em;
        border: 1px dashed ${group.props.titleColor};
        opacity: 0.5;
        flex: 1 1 10%;`;
}

export function mkContentBoxStyle(group) {
    return `
        flex: 1 1 80%;
        justify-content: center;
        border: ${group.props.showBorder ? "1px solid " + group.props.titleColor : "none"}`;
}

export function mkTitleStyle(group, hoveredGroupId) {
    return `
        text-align: center;
        font-weight: bolder;
        padding: 0 0.5em;
        ${mkColourProps(group.props.titleColor)}
        opacity: ${_.isNil(hoveredGroupId) || hoveredGroupId === group.id ? "1;" : "0.5;"}
        font-size: ${group.props.titleFontSize}em;`;
}

export function mkContainerStyle(group) {
    return `
        display: flex;
        flex-wrap: wrap;
        justify-content: center;
        height: fit-content;
        min-height: ${group.props.minHeight}em;
        ${mkColourProps(group.props.contentColor)}
        ${group.props.flexDirection === FlexDirections.ROW ? rowContainerProps : columnContainerProps}`;
}

const rowContainerProps = "flex-direction: row; align-items: flex-start; align-content: flex-start;"
const columnContainerProps = "flex-direction: column; align-items: center; align-content: center; max-height: 60em;"

export const defaultColors = [
    greyHex,
    greenHex,
    blueHex,
    purpleHex,
    redHex,
    pinkHex,
    amberHex,
    yellowHex
];


export const defaultBgColors = [
    greyBgHex,
    greenBgHex,
    blueBgHex,
    purpleBgHex,
    redBgHex,
    pinkBgHex,
    amberBgHex,
    yellowBgHex
];
