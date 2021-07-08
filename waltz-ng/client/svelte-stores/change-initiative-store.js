import {remote} from "./remote";
import {checkIsIdSelector} from "../common/checks";


export function mkChangeInitiativeStore() {

    const findForSelector = (options, force = false) => {
        checkIsIdSelector(options);
        return remote
            .fetchViewList(
                "POST",
                "api/change-initiative/selector",
                options,
                {force});
    };

    const getById = (id, force = false) => {
        return remote.fetchViewData(
            "GET",
            `api/change-initiative/id/${id}`,
            null,
            {},
            {force: force})
    }

    return {
        findForSelector,
        getById
    };
}



export const changeInitiativeStore = mkChangeInitiativeStore();