import { switchToParentIds, populateParents } from '../../common';


const BINDINGS = {
    capabilities: '=',
    explicitCapabilityIds: '=',
    initiallySelectedIds: '=',
    selectedNodes: '=',
    onSelect: '='
};


function hasChildren(node) {
    return node.children && node.children.length > 0;
}

function calcChildrenIds(node) {
    if (! hasChildren(node)) return [];

    const childIds = [];

    const add = (nodes) => {
        _.each(nodes, n => {
            childIds.push(n.id);
            if (hasChildren(n)) {
                add(n.children);
            }
        });
    };

    add([node]);
    return childIds;
}

function controller($scope) {

    const vm = this;
    vm.treeOptions = {
        nodeChildren: "children",
        multiSelection: true,
        dirSelectable: true,
        isSelectable: (n) => _.contains(vm.explicitCapabilityIds, n.id),
        equality: (n1, n2) => (n1 && n2 ? n1.id === n2.id : false),
        expanded: [1]
    };
    vm.treeData = [];

    $scope.$watchGroup(
        ['ctrl.capabilities', 'ctrl.explicitCapabilityIds', 'ctrl.initiallySelectedIds'],
        ([capabilities, explicitCapabilityIds = [], initiallySelectedIds = []]) => {
            console.log('watch', initiallySelectedIds);

            const nodeData = switchToParentIds(populateParents(capabilities));
            const nodesById = _.indexBy(nodeData, 'id');

            vm.treeData = _.filter(nodeData, n => ! n.parentId);
            vm.expandedNodes = [];
            vm.explicitCapabilityIds = explicitCapabilityIds;
            vm.nodesById = nodesById;

            _.each(initiallySelectedIds, id => vm.selectedNodes.push(nodesById[id]));
    });

    vm.showBulkControls = (node) => {
        return hasChildren(node);
    };

    vm.selectAll = (node, $event) => {
        const childIds = calcChildrenIds(node);
        $event.stopPropagation();
        _.each(childIds, childId => {
            const selectedIds = _.map(vm.selectedNodes, 'id');
            const idx = _.indexOf(selectedIds, childId);
            if (idx === -1) {
                vm.selectedNodes.push(vm.nodesById[childId]);
            }
        });
    };

    vm.deselectAll = (node, $event) => {
        $event.stopPropagation();
        const childIds = calcChildrenIds(node);
        _.each(childIds, childId => {
            const selectedIds = _.map(vm.selectedNodes, 'id');
            const idx = _.indexOf(selectedIds, childId);
            if (idx > -1) {
                vm.selectedNodes.splice(idx, 1);
            }
        });

    };

}

controller.$inject = [ '$scope' ];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./capability-multi-selector.html')
});
