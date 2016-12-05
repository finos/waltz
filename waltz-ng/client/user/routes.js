import activeUsersView from "./active-users";
import userManagementView from "./user-management";
import userLogView from "./user-log";

const base = {
    url: 'user'
};


const activeUsersState = {
    url: '/active-users',
    views: {'content@': activeUsersView }
};


const userManagementState = {
    url: '/management',
    views: {'content@': userManagementView }
};


const userLogState = {
    url: '/log',
    views: {'content@': userLogView }
};


function configureStates(stateProvider) {
    stateProvider
        .state('main.user', base)
        .state('main.user.active', activeUsersState)
        .state('main.user.management', userManagementState)
        .state('main.user.log', userLogState);
}


configureStates.$inject = ['$stateProvider'];


export default configureStates;