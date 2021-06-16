import {remote} from "./remote";
import {checkIsIdSelector} from "../common/checks";


export function mkMeasurableCategoryAlignmentViewStore() {
    const findAlignmentsByAppSelectorRoute = (selector, force) => {
        checkIsIdSelector(selector);
        return remote
            .fetchViewList(
                "POST",
                "api/measurable-category-alignment-view/selector",
                selector,
                {force});
    }

    return {
        findAlignmentsByAppSelectorRoute
    };
}



export const measurableCategoryAlignmentViewStore = mkMeasurableCategoryAlignmentViewStore();