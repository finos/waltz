import {remote, remoteApp} from "./remote";


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


    const findByEntityReference = (ref, force) => remote
        .fetchViewList(
            "GET",
            `api/assessment-definition/kind/${ref.kind}/id/${ref.id}`,
            null,
            {force});


    const save = (def) => {
        return remote
            .execute(
                "PUT",
                `api/assessment-definition`,
                def);
    };


    const remove = (id) => remote
        .execute(
            "DELETE",
            `api/assessment-definition/id/${id}`);


    // --- FAVOURITES (note use of remoteApp)

    const addFavourite = (defId) => remoteApp
            .execute(
                "PUT",
                `api/assessment-definition/id/${defId}/favourite`,
                null);


    const findFavouritesForUser = (force) => remoteApp
        .fetchViewList(
            "GET",
            "api/assessment-definition/favourites",
            null,
            {force});

    const removeFavourite = (defId) => remoteApp
            .execute(
                "DELETE",
                `api/assessment-definition/id/${defId}/favourite`,
                null);

    return {
        loadAll,
        getById,
        findByEntityReference,
        save,
        remove,
        findFavouritesForUser,
        addFavourite,
        removeFavourite,
    };
}


export const assessmentDefinitionStore = mkAssessmentDefinitionStore();