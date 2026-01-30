import { remote } from "./remote";

function mkArchitectureRequiredChangeStore() {

    const baseUrl = `api/architecture-required-change`;

    const getById = (id, force = false) => {
        return remote
            .fetchViewDatum(
                "GET",
                `${baseUrl}/id/${id}`,
                null,
                {force});
    };

    const findForLinkedEntity = (ref, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `${baseUrl}/linked-entity/${ref.kind}/${ref.id}`,
                null,
                {force});
    };

    const findForLinkedEntityHierarchy = (ref, force = false) => {
        return remote
            .fetchViewList(
                "GET",
                `${baseUrl}/linked-entity-hierarchy/${ref.kind}/${ref.id}`,
                null,
                {force});
    };

    return {
        findForLinkedEntity,
        findForLinkedEntityHierarchy,
        getById
    };
}

export const architectureRequiredChangeStore = mkArchitectureRequiredChangeStore();
