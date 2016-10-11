const bindings = {
    trees: '<',
    onSelection: '<'
};


const template = require('./data-type-tree.html');


function controller() {
    const vm = this;

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;