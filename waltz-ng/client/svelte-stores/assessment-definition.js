import {remote} from "./remote";


export function mkAssessmentDefinitionStore() {
    const loadAll = (force) => remote
        .fetchViewList(
            "GET",
            `api/assessment-definition`,
            null,
            {force});
    const save = (def) => remote
        .execute(
            "PUT",
            `api/assessment-definition`,
            def);
    const remove = (id) => remote
        .execute(
            "DELETE",
            `api/assessment-definition/id/${id}`);

    return {
        loadAll,
        save,
        remove,
    };
}


export const assessmentDefinitionStore = mkAssessmentDefinitionStore();