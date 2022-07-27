import {remote} from "./remote";


export function mkAssessmentDefinitionStore() {
    const loadAll = (force) => remote
        .fetchAppList(
            "GET",
            "api/assessment-definition",
            null,
            {force});

    const getById = (id, force) => remote
        .fetchViewDatum(
            "GET",
            `api/assessment-definition/id/${id}`,
            null,
            {force});

    const save = (def) => {

        return remote
            .execute(
                "PUT",
                `api/assessment-definition`,
                def);
    }

    const remove = (id) => remote
        .execute(
            "DELETE",
            `api/assessment-definition/id/${id}`);

    return {
        loadAll,
        getById,
        save,
        remove,
    };
}


export const assessmentDefinitionStore = mkAssessmentDefinitionStore();