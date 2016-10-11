const baseState = {
};


const viewState = {
    url: 'physical-data-article/{id:int}',
    views: {'content@': require('./physical-data-article-view') },
};


function setup($stateProvider) {

    $stateProvider
        .state('main.physical-data-article', baseState)
        .state('main.physical-data-article.view', viewState);
}

setup.$inject = ['$stateProvider'];


export default setup;