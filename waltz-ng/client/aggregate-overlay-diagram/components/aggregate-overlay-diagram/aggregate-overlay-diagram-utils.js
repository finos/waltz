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
    const widget = writable(null);
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
    const filterParameters = writable(null);
    const widgetParameters = writable(null);
    const selectionOptions = writable(null);
    const remoteMethod = writable(null);

    //widget parameters
    const appCountSliderValue = writable(0);
    const costSliderValue = writable(0);
    const selectedAssessmentDefinition = writable(null);
    const selectedAllocationScheme = writable(null);
    const selectedCostKinds = writable([]);


    //anything passed up to endpoint
    const overlayDataCall = derived(
        [remoteMethod, selectedDiagram, selectionOptions, filterParameters, widgetParameters],
        ([$remoteMethod, $selectedDiagram, $selectionOptions, $filterParameters, $widgetParameters]) => {

            if ($remoteMethod && $selectedDiagram && $widgetParameters) {

                const assessmentBasedSelectionFilter = {
                    definitionId: $filterParameters?.assessmentDefinition.id,
                    ratingIds: _.map($filterParameters?.ratingSchemeItems, p => p?.id)
                }

                const body = Object.assign(
                    {},
                    {
                        idSelectionOptions: $selectionOptions,
                        overlayParameters: $widgetParameters
                    },
                    $filterParameters ? {assessmentBasedSelectionFilter} : null);

                return $remoteMethod($selectedDiagram.id, body);
            }
        });

    overlayDataCall.subscribe(callStore => callStore?.subscribe(d => overlayData.set(d?.data)));

    setContext("hoveredCallout", hoveredCallout);
    setContext("selectedDiagram", selectedDiagram);
    setContext("selectedInstance", selectedInstance);
    setContext("callouts", callouts);
    setContext("selectedCallout", selectedCallout);
    setContext("overlayData", overlayData);
    setContext("widget", widget);
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
    setContext("appCountSliderValue", appCountSliderValue);
    setContext("costSliderValue", costSliderValue);
    setContext("selectedAssessmentDefinition", selectedAssessmentDefinition);
    setContext("selectedAllocationScheme", selectedAllocationScheme);
    setContext("selectedCostKinds", selectedCostKinds);
    setContext("filterParameters", filterParameters);
    setContext("widgetParameters", widgetParameters);
    setContext("selectionOptions", selectionOptions);
    setContext("remoteMethod", remoteMethod);
    setContext("overlayDataCall", overlayDataCall);

    return {
        selectedDiagram,
        selectedInstance,
        callouts,
        hoveredCallout,
        selectedCallout,
        overlayData,
        widget,
        focusWidget,
        svgDetail,
        instances,
        diagramProportion,
        selectedCellId,
        selectedCellCallout,
        hasEditPermissions,
        selectedOverlay,
        relatedBackingEntities,
        appCountSliderValue,
        costSliderValue,
        selectedAssessmentDefinition,
        selectedAllocationScheme,
        selectedCostKinds,
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