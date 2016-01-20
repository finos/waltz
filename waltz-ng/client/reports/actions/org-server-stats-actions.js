import * as types from '../constants/action-types';
import _ from 'lodash';


function requestOrgServerStats(orgId) {
    return {
        type: types.REQUEST_ORG_SERVER_STATS,
        orgId
    };
}


function receiveOrgServerStats(stats) {
    return {
        type: types.RECEIVE_ORG_SERVER_STATS,
        stats,
    };
}


function asyncService($http, BaseApiUrl) {

    const base = `${BaseApiUrl}/server-info`;

    function fetchOrgServerStats(orgId) {
        return dispatch => {
            dispatch(requestOrgServerStats(orgId));
            return $http.get(`${base}/org-unit/${orgId}/stats`)
                .then(res => res.data)
                .then(stats => dispatch(receiveOrgServerStats(stats)));
        };
    }

    return {
        fetchOrgServerStats
    };
}

asyncService.$inject = ['$http', 'BaseApiUrl'];


export default asyncService;
