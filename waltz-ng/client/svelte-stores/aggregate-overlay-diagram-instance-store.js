import {remote} from "./remote";


export function mkOverlayDiagramInstanceStore() {

    const findAll = (diagramId, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                "api/aggregate-overlay-diagram-instance/all",
                null,
                {force});
    };


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
            .execute(
                "POST",
                "api/aggregate-overlay-diagram-instance/create",
                createCmd);
    };

    return {
        findAll,
        findByDiagramId,
        getById,
        create
    };
}


export const aggregateOverlayDiagramInstanceStore = mkOverlayDiagramInstanceStore();