import * as types from '../constants/action-types';

function people(state = {}, action) {
    switch (action.type) {
        case types.REQUEST_PERSON:
            return Object.assign({}, state, { [action.empId]: {} });
        case types.RECEIVE_PERSON:
            return Object.assign({}, state, { [action.empId]: action.person});
        default:
            return state;
    }
}


export default people;
