import {remote} from "./remote";


export function mkOverlayDiagramCalloutStore() {

    const findCalloutsByDiagramInstanceId = (diagramInstanceId, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `api/aggregate-overlay-diagram-callout/diagram-instance-id/${diagramInstanceId}`,
                [],
                {force});
    };

    const create = (createCommand, force = false) => {
        return remote
            .execute(
                "POST",
                "api/aggregate-overlay-diagram-callout/create",
                createCommand,
                {force});
    };


    const update = (updateCommand) => {
        return remote
            .execute(
                "POST",
                "api/aggregate-overlay-diagram-callout/update",
                updateCommand);
    };


    const remove = (id) => {
        return remote
            .execute(
                "DELETE",
                `api/aggregate-overlay-diagram-callout/remove/id/${id}`);
    };


    return {
        findCalloutsByDiagramInstanceId,
        create,
        update,
        remove
    };
}


export const aggregateOverlayDiagramCalloutStore = mkOverlayDiagramCalloutStore();