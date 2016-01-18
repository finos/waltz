import * as types from '../constants/action-types';
import _ from 'lodash';


function requestPerson(empId) {
    return {
        type: types.REQUEST_PERSON,
        empId
    };
}

function receivePerson(empId, person) {
    return {
        type: types.RECEIVE_PERSON,
        empId,
        person
    };
}

function asyncService(personStore) {
    function fetchPerson(empId) {
        return dispatch => {
            dispatch(requestPerson(empId));
            return personStore.getByEmployeeId(empId)
                .then(person => dispatch(receivePerson(empId, person)));
        };
    }

    return {
        fetchPerson
    };
}

asyncService.$inject = ['PersonDataService'];


export default asyncService;
