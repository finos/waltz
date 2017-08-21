export function store($http, baseApiUrl) {
    const base = `${baseApiUrl}/attestation-instance`;

    const attestInstance = (id) => {
        return $http
            .post(`${base}/attest/${id}`);
    };

    const findByRunId = (id) => {
        return $http
            .get(`${base}/run/${id}`)
            .then(result => result.data);
    };

    const findByUser = () => {
        return $http
            .get(`${base}/user`)
            .then(result => result.data);
    };

    const findPersonsById = (id) => {
        return $http
            .get(`${base}/${id}/person`)
            .then(result => result.data);
    };

    return {
        attestInstance,
        findByRunId,
        findByUser,
        findPersonsById
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'AttestationInstanceStore';


export const AttestationInstanceStore_API = {
    attestInstance: {
        serviceName,
        serviceFnName: 'attestInstance',
        description: 'create an attestation'
    },
    findByRunId: {
        serviceName,
        serviceFnName: 'findByRunId',
        description: 'find attestations by a run id'
    },
    findByUser: {
        serviceName,
        serviceFnName: 'findByUser',
        description: 'find attestations for a user'
    },
    findPersonsById: {
        serviceName,
        serviceFnName: 'findPersonsById',
        description: 'find recipients (person) for an instance'
    }
};