export function store($http, baseApiUrl) {
    const base = `${baseApiUrl}/attestation-instance`;

    const findByUser = () => {
        return $http
            .get(`${base}/user`)
            .then(result => result.data);
    };

    const attestInstance = (id) => {
        return $http
            .post(`${base}/attest/${id}`);
    };

    return {
        findByUser,
        attestInstance,
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'AttestationInstanceStore';


export const AttestationInstanceStore_API = {
    findByUser: {
        serviceName,
        serviceFnName: 'findByUser',
        description: 'find attestations for a user'
    },
    attestInstance: {
        serviceName,
        serviceFnName: 'attestInstance',
        description: 'create an attestation'
    }
};