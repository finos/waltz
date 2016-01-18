import * as types from '../constants/action-types';

function appsByEmpId(state = {}, action) {
    switch (action.type) {
        case types.REQUEST_PERSON_PORTFOLIO:
            return Object.assign({}, state, { [action.empId]: []});
        case types.RECEIVE_PERSON_PORTFOLIO:
            return Object.assign({}, state, { [action.empId]: action.applications});
        default:
            return state;
    }
}


export default appsByEmpId;
