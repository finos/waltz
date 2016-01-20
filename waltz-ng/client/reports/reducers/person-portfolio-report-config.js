import * as types from '../constants/action-types';

function reportConfigs(state = {}, action) {
    switch (action.type) {
        case types.REQUEST_PERSON_PORTFOLIO_REPORT_CONFIG:
            return Object.assign({}, state, { [action.reportId]: []});
        case types.RECEIVE_PERSON_PORTFOLIO_REPORT_CONFIG:
            return Object.assign({}, state, { [action.reportId]: action.reportConfig});
        default:
            return state;
    }
}


export default reportConfigs;
