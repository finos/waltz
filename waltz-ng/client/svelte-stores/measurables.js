import {remote} from "./remote";
import {checkIsIdSelector} from "../common/checks";


export function mkMeasurableStore() {
    const loadAll = (force) => remote
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
                "api/measurable/measurable-selector",
                options,
                {force});
    };

    const getById = (id, force = false) => {
        return remote.fetchViewData(
            "GET",
            `api/measurable/id/${id}`,
            null,
            {},
            {force: force});
    }

    const findByParentId = (id, force = false) => {
        return remote.fetchViewData(
            "GET",
            `api/measurable/parent-id/${id}`,
            null,
            {},
            {force: force});
    }

    return {
        loadAll,
        findMeasurablesBySelector,
        getById,
        findByParentId
    };
}



export const measurableStore = mkMeasurableStore();