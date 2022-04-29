import {remote} from "./remote";
import {checkIsEntityRef} from "../common/checks";


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


    const update = (updateCommand, force = false) => {
        return remote
            .execute(
                "POST",
                "api/aggregate-overlay-diagram-callout/update",
                updateCommand,
                {force});
    };


    return {
        findCalloutsByDiagramInstanceId,
        create,
        update
    };
}


export const aggregateOverlayDiagramCalloutStore = mkOverlayDiagramCalloutStore();