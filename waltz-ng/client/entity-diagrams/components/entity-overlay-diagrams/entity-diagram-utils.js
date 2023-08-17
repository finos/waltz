import {
    amberBgHex,
    amberHex,
    blueBgHex,
    blueHex,
    determineForegroundColor,
    greenBgHex,
    greenHex,
    greyBgHex,
    greyHex,
    pinkBgHex,
    pinkHex,
    purpleBgHex,
    purpleHex,
    redBgHex,
    redHex,
    yellowBgHex,
    yellowHex
} from "../../../common/colors";
import _ from "lodash";
import {entity} from "../../../common/services/enums/entity";
import {
    mkAggregatedEntitiesGlobalProps, mkAppChangesOverlayGlobalProps,
    mkAssessmentOverlayGlobalProps
} from "../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/aggregate-overlay-diagram-utils";
import DefaultOverlay from "./overlays/DefaultOverlay.svelte";
import AggregatedEntitiesOverlayParameters from "./overlays/AggregatedEntitiesOverlayParameters.svelte";
import DefaultOverlayParameters from "./overlays/DefaultOverlayParameters.svelte";
import AggregatedEntitiesOverlayCell
    from "../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/widgets/aggregated-entities/AggregatedEntitiesOverlayCell.svelte";
import BackingEntitiesOverlayCell
    from "../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/widgets/backing-entities/BackingEntitiesPlainOverlayCell.svelte";
import {
    resetParameters as resetAssessmentParameters
} from "../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/widgets/assessments/AssessmentWidgetParameters.svelte";
import AssessmentOverlayParameters from "./overlays/assessment/AssessmentOverlayParameters.svelte";
import AssessmentOverlay from "./overlays/assessment/AssessmentOverlay.svelte";
import AssessmentOverlayLegendDetail from "./overlays/assessment/AssessmentOverlayLegendDetail.svelte";
import ApplicationChangesOverlay from "./overlays/ApplicationChangesOverlay.svelte";
import ApplicationChangesOverlayParameters from "./overlays/ApplicationChangesOverlayParameters.svelte";

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

function mkFlexProportion(group, child) {
    return `flex: ${child.props.proportion} 1 ${_.floor(100 / (group.props.bucketSize + 1))}%;`;
}


export function mkGroupStyle(group, child) {
    return `
        ${mkFlexProportion(group, child)}
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
        ${mkColourProps(group.props.showTitle ? group.props.titleColor : group.props.contentColor)}
        opacity: ${_.isNil(hoveredGroupId) || hoveredGroupId === group.id ? "1;" : "0.5;"}
        font-size: ${group.props.showTitle ? group.props.titleFontSize : group.props.contentFontSize}em;`;
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

export const defaultOverlay = {
    key: "CELL_DATA",
    name: "Cell Data",
    icon: "cubes",
    description: "Displays the cell data that populates the cell",
    component: DefaultOverlay,
    aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key],
    parameterWidget: DefaultOverlayParameters,
    showTitle: false
};

export const overlays =  [
    {
        key: "AGGREGATED_ENTITIES",
        name: "Aggregated Entities",
        icon: "pie-chart",
        description: "Displays entities which are aggregated to populate the overlay data",
        component: AggregatedEntitiesOverlayCell,
        url: "aggregated-entities-widget",
        mkGlobalProps: mkAggregatedEntitiesGlobalProps,
        aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key],
        parameterWidget: AggregatedEntitiesOverlayParameters,
        showTitle: true
    },
    {
        key: "BACKING_ENTITIES",
        name: "Backing Entities",
        icon: "cubes",
        description: "Displays the underlying entities which drive the overlays on the diagram",
        component: BackingEntitiesOverlayCell,
        url: "backing-entity-widget",
        aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key],
        parameterWidget: DefaultOverlayParameters,
        showTitle: false
    },
    {
        key: "ASSESSMENTS",
        name: "Assessments",
        icon: "puzzle-piece",
        description: "Allows user to select an assessment to overlay on the diagram",
        component: AssessmentOverlay,
        url: "app-assessment-widget",
        mkGlobalProps: mkAssessmentOverlayGlobalProps,
        resetParameters: resetAssessmentParameters,
        aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key],
        parameterWidget: AssessmentOverlayParameters,
        legend: AssessmentOverlayLegendDetail,
        showTitle: true
    },
    {
        key: "APPLICATION_CHANGE",
        name: "Application Change",
        icon: "desktop",
        description: "Displays the incoming and outgoing applications based upon app retirements, rating decomms and replacements",
        component: ApplicationChangesOverlay,
        url: "app-change-widget",
        mkGlobalProps: mkAppChangesOverlayGlobalProps,
        resetParameters: resetAssessmentParameters,
        aggregatedEntityKinds: [entity.APPLICATION.key],
        parameterWidget: ApplicationChangesOverlayParameters,
        showTitle: true
    }
]

