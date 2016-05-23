import * as types from "../constants/action-types";
import _ from "lodash";


function requestPersonPortfolio(empId) {
    return {
        type: types.REQUEST_PERSON_PORTFOLIO,
        empId
    };
}

function requestPersonPortfolioReportConfig(reportId) {
    return {
        type: types.REQUEST_PERSON_PORTFOLIO_REPORT_CONFIG,
        reportId
    };
}


function receivePersonPortfolio(empId, applications) {
    return {
        type: types.RECEIVE_PERSON_PORTFOLIO,
        empId,
        applications
    };
}


function receivePersonPortfolioReportConfig(reportId, reportConfig) {
    return {
        type: types.RECEIVE_PERSON_PORTFOLIO_REPORT_CONFIG,
        reportId,
        reportConfig
    };
}


function asyncService(involvementStore, personActions) {
    function fetchPersonPortfolio(empId) {
        return dispatch => {
            dispatch(requestPersonPortfolio(empId));
            return involvementStore.findAppsForEmployeeId(empId)
                .then(apps => dispatch(receivePersonPortfolio(empId, apps)));
        };
    }

    function fetchPersonPortfolioReportConfig(reportId) {
        return dispatch => {
            dispatch(requestPersonPortfolioReportConfig(reportId));

            const dummyReport = {
                id: reportId,
                name: 'GTO Competition',
                description: 'Something about this report',
                employeeIds: ['hb7m0cqg1', 'ZmdOM6ZvB', 'TqWvxU59E']
            };
            return Promise.resolve(dummyReport)
                .then(c => {
                    _.each(c.employeeIds, empId => {
                        dispatch(fetchPersonPortfolio(empId));
                        dispatch(personActions.fetchPerson(empId));
                    });
                    dispatch(receivePersonPortfolioReportConfig(reportId, c));
                });

        };
    }

    return {
        fetchPersonPortfolio,
        fetchPersonPortfolioReportConfig
    };
}

asyncService.$inject = [
    'InvolvementStore',
    'PersonActions'
];


export default asyncService;
