import editor from "./bookmark-editor";

function setup($stateProvider) {
    $stateProvider
        .state('main.bookmarks', {
        })
        .state('main.bookmarks.edit', {
            url: 'bookmarks/{kind}/{entityId:int}/edit?parentName',
            views: {'content@': editor }
        });
}

setup.$inject = ['$stateProvider'];

export default setup;