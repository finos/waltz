import {remote} from "./remote";
import {checkIsIdSelector} from "../common/checks";


export function mkMeasurableStore() {
    const loadAll = (ref, force) => remote
        .fetchViewList(
            "GET",
            `api/measurable/all`,
            null,
            {force});

    const findMeasurablesBySelector = (options, force = false) => {
        checkIsIdSelector(options);
        return remote
            .fetchViewList(
                "POST",
                `api/measurable/measurable-selector`,
                options,
                {force});
    };

    return {
        loadAll,
        findMeasurablesBySelector
    };
}



export const measurableStore = mkMeasurableStore();