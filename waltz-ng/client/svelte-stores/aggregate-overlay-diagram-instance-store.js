import {remote} from "./remote";
import {checkIsEntityRef} from "../common/checks";


export function mkOverlayDiagramInstanceStore() {

    const findByDiagramId = (diagramId, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `api/aggregate-overlay-diagram-instance/diagram-id/${diagramId}`,
                {force});
    };

    return {
        findByDiagramId,
    };
}


export const aggregateOverlayDiagramInstanceStore = mkOverlayDiagramInstanceStore();