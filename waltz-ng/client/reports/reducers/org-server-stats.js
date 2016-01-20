import * as types from '../constants/action-types';

function orgServerStats(state = {}, action) {
    switch (action.type) {
        case types.RECEIVE_ORG_SERVER_STATS:
            return Object.assign({}, {}, action.stats);
        default:
            return state;
    }
}


export default orgServerStats;
