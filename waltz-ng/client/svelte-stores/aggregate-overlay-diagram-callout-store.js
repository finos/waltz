import {remote} from "./remote";
import {checkIsEntityRef} from "../common/checks";


export function mkOverlayDiagramCalloutStore() {

    const findCalloutsByDiagramInstanceId = (diagramInstanceId, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `api/aggregate-overlay-diagram-callout/diagram-instance-id/${diagramInstanceId}`,
                {force});
    };

    return {
        findCalloutsByDiagramInstanceId,
    };
}


export const aggregateOverlayDiagramCalloutStore = mkOverlayDiagramCalloutStore();