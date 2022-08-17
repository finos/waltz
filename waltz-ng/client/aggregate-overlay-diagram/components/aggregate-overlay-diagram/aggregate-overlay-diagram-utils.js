import _ from "lodash";
import {derived, writable} from "svelte/store";
import {setContext} from "svelte";
import {
    amberHex,
    blueHex,
    goldHex,
    greenHex,
    greyHex,
    lightGreyHex,
    pinkHex,
    purpleHex,
    redHex,
    yellowHex
} from "../../../common/colors";
import {refToString} from "../../../common/entity-utils";
import TargetAppCostWidgetParameters from "./widgets/target-costs/TargetAppCostWidgetParameters.svelte";
import TargetAppCostOverlayCell from "./widgets/target-costs/TargetAppCostOverlayCell.svelte";
import {aggregateOverlayDiagramStore} from "../../../svelte-stores/aggregate-overlay-diagram-store";
import {entity} from "../../../common/services/enums/entity";
import AppCostWidgetParameters from "./widgets/app-costs/AppCostWidgetParameters.svelte";
import AppCostOverlayCell from "./widgets/app-costs/AppCostOverlayCell.svelte";
import AppCountWidgetParameters from "./widgets/app-counts/AppCountWidgetParameters.svelte";
import AppCountOverlayCell from "./widgets/app-counts/AppCountOverlayCell.svelte";
import AssessmentWidgetParameters from "./widgets/assessments/AssessmentWidgetParameters.svelte";
import AssessmentOverlayCell from "./widgets/assessments/AssessmentOverlayCell.svelte";
import AssessmentOverlayLegend from "./widgets/assessments/AssessmentOverlayLegend.svelte";
import BackingEntitiesWidgetParameters from "./widgets/backing-entities/BackingEntitiesWidgetParameters.svelte";
import BackingEntitiesOverlayCell from "./widgets/backing-entities/BackingEntitiesOverlayCell.svelte";
import AggregatedEntitiesWidgetParameters
    from "./widgets/aggregated-entities/AggregatedEntitiesWidgetParameters.svelte";
import AggregatedEntitiesOverlayCell from "./widgets/aggregated-entities/AggregatedEntitiesOverlayCell.svelte";
import {
    resetParameters as resetComplexityParameters
} from "../aggregate-overlay-diagram/widgets/complexities/ComplexityWidgetParameters.svelte";
import {
    resetParameters as resetAssessmentParameters
} from "../aggregate-overlay-diagram/widgets/assessments/AssessmentWidgetParameters.svelte";
import {
    resetParameters as resetTargetAppCostParameters
} from "../aggregate-overlay-diagram/widgets/target-costs/TargetAppCostWidgetParameters.svelte";
import {
    resetParameters as resetAppCostParameters
} from "../aggregate-overlay-diagram/widgets/app-costs/AppCostWidgetParameters.svelte";
import {
    resetParameters as resetTargetAppCountParameters
} from "../aggregate-overlay-diagram/widgets/app-counts/AppCountWidgetParameters.svelte";
import ComplexityOverlayCell from "./widgets/complexities/ComplexityOverlayCell.svelte";
import ComplexityWidgetParameters from "./widgets/complexities/ComplexityWidgetParameters.svelte";


export function clearContent(svgHolderElem, targetSelector) {
    const existingContent = svgHolderElem.querySelectorAll(`${targetSelector} .content`);
    _.each(existingContent, elem => elem.parentNode.removeChild(elem));
}


/**
 * Adds click handlers to all `.data-cell` elements.  The
 * click handler simply puts the cell id, name and any .stats svg
 * into the selectedOverlayCellStore.
 *
 * @param svgHolderElem
 * @param selectedOverlayCellStore
 * @param propsByCellId
 */
export function addCellClickHandlers(svgHolderElem, selectedOverlayCellStore, propsByCellId) {
    let dataCells = svgHolderElem.querySelectorAll(".data-cell");
    Array
        .from(dataCells)
        .forEach(sb => {
            sb.onclick = () => {
                const cellId = sb.getAttribute("data-cell-id");
                const cellName = sb.getAttribute("data-cell-name");
                const svg = sb.querySelector(".statistics-box svg");
                selectedOverlayCellStore.set({cellId, cellName, svg, props: propsByCellId[cellId]});
            };
        });
}


export function addSectionHeaderClickHandlers(svgHolderElem, selectedOverlayCellStore, propsByCellId) {
    let headerCells = svgHolderElem.querySelectorAll(".group-title");
    Array
        .from(headerCells)
        .forEach(sb => {
            sb.onclick = () => {
                const dataCell = determineCell(sb);
                const cellId = dataCell.getAttribute("data-cell-id");
                const cellName = dataCell.getAttribute("data-cell-name");
                selectedOverlayCellStore.set({cellId, cellName, props: propsByCellId[cellId]});
            };
        });
}


/**
 * Given a list of backing entities and entity references this function
 * will return a list of cellId's which are mentioned by any of the
 * linked entityReferences.
 *
 * @param backingEntities  [ { cellId, entityReference }, ... ]
 * @param relatedEntities [ { a, b, ... }, .... ]
 * @returns list of cell ids
 */
export function determineWhichCellsAreLinkedByParent(backingEntities = [],
                                                     relatedEntities = []) {
    if (backingEntities && relatedEntities) {
        const relatedRefs = _
            .chain(relatedEntities)
            .map(d => [d.a, d.b])
            .flatten()
            .map(refToString)
            .value();

        return _
            .chain(backingEntities)
            .groupBy(d => d.cellId)
            .map((xs, k) => {
                const backingRefs = _.map(xs, x => refToString(x.entityReference));
                return _.some(backingRefs, br => _.includes(relatedRefs, br))
                    ? k
                    : null;
            })
            .compact()
            .value();
    } else {
        return [];
    }

}



export function setupContextStores() {
    const selectedDiagram = writable(null);
    const selectedInstance = writable(null);
    const callouts = writable([]);
    const hoveredCallout = writable(null);
    const selectedCallout = writable(null);
    const overlayData = writable([]);
    const focusWidget = writable(null);
    const svgDetail = writable(null);
    const instances = writable([]);
    const diagramProportion = writable(9);
    const selectedCellId = writable(null);
    const selectedCellCallout = writable(null);
    const hasEditPermissions = writable(false);
    const selectedOverlay = writable(null);
    const relatedBackingEntities = writable([]);
    const cellIdsExplicitlyRelatedToParent = writable([]);
    const filterParameters = writable([]);
    const widgetParameters = writable(null);
    const selectionOptions = writable(null);
    const diagramPresets = writable([]);
    const selectedPreset = writable(null);
    const loading = writable(false);
    const disabledWidgetKeys = writable([]);
    const selectedFilter = writable(null);


    //anything passed up to endpoint
    const overlayDataCall = derived(
        [focusWidget, selectedDiagram, selectionOptions, filterParameters, widgetParameters],
        ([$focusWidget, $selectedDiagram, $selectionOptions, $filterParameters, $widgetParameters]) => {

            if ($focusWidget && $selectedDiagram && $widgetParameters) {

                const assessmentBasedSelectionFilters = _.isEmpty($filterParameters)
                    ? []
                    : _.map(
                        $filterParameters,
                        filter => {
                            const definitionId = _.get(filter, ["assessmentDefinition", "id"]);
                            const ratings = _.get(filter, ["ratingSchemeItems"], []);
                            const ratingIds = _.map(ratings, p => _.get(p, "id"));

                            return {
                                definitionId,
                                ratingIds
                            };
                        });

                const body = Object.assign(
                    {},
                    {
                        idSelectionOptions: $selectionOptions,
                        overlayParameters: $widgetParameters
                    },
                    {assessmentBasedSelectionFilters});

                return $focusWidget.remoteMethod($selectedDiagram.id, body);
            }
        });

    let dataCallUnsubscribeFn = null;

    overlayDataCall.subscribe(callStore => {
        if (dataCallUnsubscribeFn) {
            dataCallUnsubscribeFn();
        }
        dataCallUnsubscribeFn = callStore
            ? callStore.subscribe(d => {
                overlayData.set(d.data);
                loading.set(d.status === "loading")
            })
            : null;
    });


    setContext("hoveredCallout", hoveredCallout);
    setContext("selectedDiagram", selectedDiagram);
    setContext("selectedInstance", selectedInstance);
    setContext("callouts", callouts);
    setContext("selectedCallout", selectedCallout);
    setContext("overlayData", overlayData);
    setContext("focusWidget", focusWidget);
    setContext("svgDetail", svgDetail);
    setContext("instances", instances);
    setContext("diagramProportion", diagramProportion);
    setContext("selectedCellId", selectedCellId);
    setContext("selectedCellCallout", selectedCellCallout);
    setContext("hasEditPermissions", hasEditPermissions);
    setContext("selectedOverlay", selectedOverlay);
    setContext("relatedBackingEntities", relatedBackingEntities);
    setContext("cellIdsExplicitlyRelatedToParent", cellIdsExplicitlyRelatedToParent);
    setContext("filterParameters", filterParameters);
    setContext("widgetParameters", widgetParameters);
    setContext("selectionOptions", selectionOptions);
    setContext("overlayDataCall", overlayDataCall);
    setContext("diagramPresets", diagramPresets);
    setContext("selectedPreset", selectedPreset);
    setContext("loading", loading);
    setContext("disabledWidgetKeys", disabledWidgetKeys);
    setContext("selectedFilter", selectedFilter);


    return {
        selectedDiagram,
        selectedInstance,
        callouts,
        hoveredCallout,
        selectedCallout,
        overlayData,
        focusWidget,
        svgDetail,
        instances,
        diagramProportion,
        selectedCellId,
        selectedCellCallout,
        hasEditPermissions,
        selectedOverlay,
        relatedBackingEntities,
        filterParameters,
        widgetParameters
    };
}


export const calloutColors = [
    greyHex,
    lightGreyHex,
    greenHex,
    blueHex,
    purpleHex,
    redHex,
    pinkHex,
    goldHex,
    amberHex,
    yellowHex
];


export function determineCell(elem) {
    if (elem == null) {
        return null;
    } else {
        const cellId = elem.getAttribute("data-cell-id");
        if (!_.isNil(cellId)) {
            return elem;
        } else {
            return determineCell(elem.parentElement)
        }
    }
}


export function mkAggregatedEntitiesGlobalProps(data) {
    const maxCount = _
        .chain(data.cellData)
        .map(d => _.size(d.aggregatedEntityReferences))
        .max()
        .value();
    return {maxCount};
}


export function mkAssessmentOverlayGlobalProps(data) {
    const maxCount = _
        .chain(data.cellData)
        .map(d => d.counts)
        .flatten()
        .map(d => _.get(d, ["count"], 0))
        .max()
        .value();

    return {maxCount};
}


export function mkComplexityOverlayGlobalProps(data) {
    const maxTotalComplexity = _
        .chain(data.cellData)
        .map(d => _.get(d, ["totalComplexity"], 0))
        .max()
        .value();

    const maxAverageComplexity = _
        .chain(data.cellData)
        .map(d => _.get(d, ["averageComplexity"], 0))
        .max()
        .value();

    return {
        maxTotalComplexity,
        maxAverageComplexity,
        applicationsById: _.keyBy(data.applications, d => d.id),
        complexityKindsById: _.keyBy(data.complexityKinds, d => d.id)
    };
}


export function mkTargetAppCountGlobalProps(data) {
    const maxCount = _
        .chain(data.cellData)
        .map(d => [_.get(d, ["currentStateCount"], 0), _.get(d, ["targetStateCount"], 0)])
        .flatten()
        .max()
        .value();
    return {maxCount};
}


export function mkAppCostGlobalProps(data) {

    const maxCost = _
        .chain(data.cellData)
        .map(d => _.get(d, ["totalCost"], 0))
        .max()
        .value();

    return {
        maxCost,
        applicationsById: _.keyBy(data.applications, d => d.id),
        measurablesById: _.keyBy(data.measurables, d => d.id),
        costKindsById: _.keyBy(data.costKinds, d => d.id)
    };
}


export function mkTargetAppCostGlobalProps(data) {
    const maxCost = _
        .chain(data.cellData)
        .map(d => [_.get(d, ["currentStateCost"], 0), _.get(d, ["targetStateCost"], 0)])
        .flatten()
        .max()
        .value();

    return {
        maxCost,
    };
}


export const widgets = [
    {
        key: "TARGET_APP_COSTS",
        parameterWidget: TargetAppCostWidgetParameters,
        description: "Shows current cost and future cost info",
        label: "Target App Costs",
        icon: "money",
        overlay: TargetAppCostOverlayCell,
        remoteMethod: aggregateOverlayDiagramStore.findTargetAppCostForDiagram,
        mkGlobalProps: mkTargetAppCostGlobalProps,
        resetParameters: resetTargetAppCostParameters,
        aggregatedEntityKinds: [entity.APPLICATION.key],
    }, {
        key: "APP_COSTS",
        parameterWidget: AppCostWidgetParameters,
        description: "Shows current app costs accounting for allocation percentages",
        label: "App Costs",
        icon: "money",
        overlay: AppCostOverlayCell,
        remoteMethod: aggregateOverlayDiagramStore.findAppCostForDiagram,
        mkGlobalProps: mkAppCostGlobalProps,
        resetParameters: resetAppCostParameters,
        aggregatedEntityKinds: [entity.APPLICATION.key]
    }, {
        key: "TARGET_APP_COUNTS",
        parameterWidget: AppCountWidgetParameters,
        description: "Shows current app count and future app count info",
        label: "App Counts",
        icon: "desktop",
        overlay: AppCountOverlayCell,
        remoteMethod: aggregateOverlayDiagramStore.findAppCountsForDiagram,
        mkGlobalProps: mkTargetAppCountGlobalProps,
        resetParameters: resetTargetAppCountParameters,
        aggregatedEntityKinds: [entity.APPLICATION.key]
    }, {
        key: "ASSESSMENTS",
        label: "Assessments",
        icon: "puzzle-piece",
        description: "Allows user to select an assessment to overlay on the diagram",
        parameterWidget: AssessmentWidgetParameters,
        overlay: AssessmentOverlayCell,
        legend: AssessmentOverlayLegend,
        remoteMethod: aggregateOverlayDiagramStore.findAppAssessmentsForDiagram,
        mkGlobalProps: mkAssessmentOverlayGlobalProps,
        resetParameters: resetAssessmentParameters,
        aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key]
    }, {
        key: "COMPLEXITIES",
        label: "Complexity Scores",
        icon: "puzzle-piece",
        description: "Allows user to select an complexity statistic to overlay on the diagram",
        parameterWidget: ComplexityWidgetParameters,
        overlay: ComplexityOverlayCell,
        remoteMethod: aggregateOverlayDiagramStore.findComplexitiesForDiagram,
        mkGlobalProps: mkComplexityOverlayGlobalProps,
        resetParameters: resetComplexityParameters,
        aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key]
    }, {
        key: "BACKING_ENTITIES",
        label: "Backing Entities",
        icon: "cubes",
        description: "Displays the underlying entities which drive the overlays on the diagram",
        parameterWidget: BackingEntitiesWidgetParameters,
        overlay: BackingEntitiesOverlayCell,
        remoteMethod: aggregateOverlayDiagramStore.findBackingEntitiesForDiagram,
        aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key]
    }, {
        key: "AGGREGATED_ENTITIES",
        label: "Aggregated Entities",
        icon: "pie-chart",
        description: "Displays entities which are aggregated to populate the overlay data",
        parameterWidget: AggregatedEntitiesWidgetParameters,
        overlay: AggregatedEntitiesOverlayCell,
        remoteMethod: aggregateOverlayDiagramStore.findAggregatedEntitiesForDiagram,
        mkGlobalProps: mkAggregatedEntitiesGlobalProps,
        aggregatedEntityKinds: [entity.APPLICATION.key, entity.CHANGE_INITIATIVE.key]
    }
];


export const RenderModes = {
    OVERLAY: Symbol("OVERLAY"),
    FOCUSED: Symbol("FOCUSED")
};