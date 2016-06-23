import _ from "lodash";


const BINDINGS = {
    app: '<',
    tags: '<',
    aliases: '<',
    organisationalUnit: '<',
    complexity: '<',
    updateAliases: '&'
};


const initialState = {
    visibility: {
        aliasEditor: false
    },
    fieldEditor: {
        aliases: []
    }
};


function controller($state) {
    const vm = _.defaultsDeep(this, initialState);

    vm.showAliasEditor = () => {
        vm.visibility.aliasEditor = true;
        vm.fieldEditor.aliases = vm.aliases;
    };

    vm.dismissAliasEditor = () => {
        vm.visibility.aliasEditor = false;
        vm.fieldEditor.aliases = [];
    };

    vm.tagSelected = (tag) => {
        $state.go('main.app.tag-explorer', { tag });
    };

    vm.saveTags = () => {
        vm.updateAliases({ aliases: vm.fieldEditor.aliases })
            .then(() => vm.dismissAliasEditor());
    };

}

controller.$inject = ['$state'];


const directive = {
    restrict: 'E',
    replace: false,
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl',
    template: require('./app-overview.html')
};


export default () => directive;
