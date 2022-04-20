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


    const getById = (id, force = false) => {
        return remote
            .fetchViewDatum(
                "GET",
                `api/aggregate-overlay-diagram-instance/id/${id}`,
                {force});
    };

    return {
        findByDiagramId,
        getById
    };
}


export const aggregateOverlayDiagramInstanceStore = mkOverlayDiagramInstanceStore();