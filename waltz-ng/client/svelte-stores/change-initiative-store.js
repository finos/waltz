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

    return {
        findForSelector
    };
}



export const changeInitiativeStore = mkChangeInitiativeStore();