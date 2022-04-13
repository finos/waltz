import {remote} from "./remote";
import {checkIsEntityRef} from "../common/checks";


export function mkOverlayDiagramStore() {

    const findAppCountsForDiagram = (diagramId, vantagePointRef, force = false) => {
        return remote
            .fetchViewList(
                "POST",
                `api/overlay-diagram/diagram-id/${diagramId}/app-count-widget`,
                vantagePointRef,
                {force});
    };

    const findAppCostForDiagram = (diagramId, vantagePointRef, force = false) => {
        return remote
            .fetchViewList(
                "POST",
                `api/overlay-diagram/diagram-id/${diagramId}/app-cost-widget`,
                vantagePointRef,
                {force});
    };

    return {
        findAppCountsForDiagram,
        findAppCostForDiagram
    };
}


export const overlayDiagramStore = mkOverlayDiagramStore();