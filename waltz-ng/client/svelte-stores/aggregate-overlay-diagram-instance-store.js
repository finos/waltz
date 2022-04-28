import {remote} from "./remote";
import {checkIsEntityRef} from "../common/checks";


export function mkOverlayDiagramInstanceStore() {

    const findByDiagramId = (diagramId, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `api/aggregate-overlay-diagram-instance/diagram-id/${diagramId}`,
                null,
                {force});
    };


    const getById = (id, force = false) => {
        return remote
            .fetchViewDatum(
                "GET",
                `api/aggregate-overlay-diagram-instance/id/${id}`,
                null,
                {force});
    };


    const create = (createCmd, force = false) => {
        return remote
            .fetchViewDatum(
                "POST",
                "api/aggregate-overlay-diagram-instance/create",
                createCmd,
                {force});
    };

    return {
        findByDiagramId,
        getById,
        create
    };
}


export const aggregateOverlayDiagramInstanceStore = mkOverlayDiagramInstanceStore();