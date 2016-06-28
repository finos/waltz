import ProfileView from "./view";

const baseState = {
    url: 'profile'
};


const viewState = {
    url: '/view/{userId:string}',
    views: { 'content@': ProfileView }
};


function setup($stateProvider) {
    $stateProvider
        .state('main.profile', baseState)
        .state('main.profile.view', viewState);
}

setup.$inject = ['$stateProvider'];


export default setup;