const BINDINGS = {
    kind: '=',
    bookmarks: '='
};


function controller() {
}


const directive = {
    restrict: 'E',
    replace: true,
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl',
    template: require('./bookmark-card.html')
};


export default () => directive;
