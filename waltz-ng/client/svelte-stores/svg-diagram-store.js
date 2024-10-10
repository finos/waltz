import {remote} from "./remote";


export function mkSvgDiagramStore() {

    const findAll = (force) => remote
        .fetchAppList(
            "GET",
            "api/svg-diagram",
            [],
            {force});

    const getById = (diagramId, force) => remote
        .fetchAppList(
            "GET",
            `api/svg-diagram/${diagramId}`,
            [],
            {force});

    const save = (diagram) => remote
        .execute(
            "POST",
            "api/svg-diagram/save",
            diagram);

    const remove = (diagramId) => remote
        .execute(
            "DELETE",
            `api/svg-diagram/${diagramId}`);

    return {
        findAll,
        getById,
        save,
        remove
    };
}


export const svgDiagramStore = mkSvgDiagramStore();