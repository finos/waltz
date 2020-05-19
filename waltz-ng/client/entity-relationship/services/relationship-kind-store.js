export function store($http, baseUrl) {

    const BASE = `${baseUrl}/relationship-kind`;

    const findAll = () => {
        return $http.get(`${BASE}`)
            .then(result => result.data);
    };

    const findRelationshipKindsBetweenEntities = (parentRef, targetRef) => {
        return $http.get(`${BASE}/entities/${parentRef.kind}/${parentRef.id}/${targetRef.kind}/${targetRef.id}`)
            .then(result => result.data);
    };

    return {
        findAll,
        findRelationshipKindsBetweenEntities
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "RelationshipKindStore";


export const RelationshipKindStore_API = {
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "executes findAll"
    },
    findRelationshipKindsBetweenEntities: {
        serviceName,
        serviceFnName: "findRelationshipKindsBetweenEntities",
        description: "finds allowed relationship kinds between entity a and entity b"
    }
};