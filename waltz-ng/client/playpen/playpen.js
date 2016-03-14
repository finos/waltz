import _ from 'lodash';

import { switchToParentIds, populateParents } from '../common';

function controller($stateParams, $q, capabilityStore, appCapabilityStore) {
    const vm = this;

    const id = $stateParams.id;

    vm.onAdd = (d) => console.log("On Add", d);

    vm.treeOptions = {
        nodeChildren: "children",
        multiSelection: true,
        dirSelectable: true,
        isSelectable: (n) => _.contains(vm.explicitCapabilityIds, n.id),
        equality: (n1, n2) => (n1 && n2 ? n1.id === n2.id : false),
        expanded: [1]
    };
    vm.treeData = [];

    vm.onSelection = (n, selected) => console.log('onSelection', n, selected);

    const promises = [
        capabilityStore.findAll(),
        appCapabilityStore.findApplicationCapabilitiesByAppIds([id])
    ];

    $q.all(promises).then(([capabilities, appCapabilities]) => {
        vm.capabilities = capabilities;
        vm.appCapabilities = appCapabilities;

        const capabilitiesById = _.indexBy(capabilities, 'id');

        const explicitCapabilityIds = _.map(appCapabilities, 'capabilityId');
        const primaryCapabilityIds = _.chain(appCapabilities)
            .filter('primary')
            .map('capabilityId')
            .value();

        const capabilitiesForApp = _.chain(explicitCapabilityIds)
            .map(cId => capabilitiesById[cId])
            .compact()
            .map(c => ([c.level1, c.level2, c.level3, c.level4, c.level5]))
            .flatten()
            .compact()
            .uniq()
            .map(cId => capabilitiesById[cId])
            .compact()
            .value();


        const nodeData = switchToParentIds(populateParents(capabilitiesForApp));
        const nodesById = _.indexBy(nodeData, 'id');

        const expandedNodes = _.chain(primaryCapabilityIds)
            .map(cId => capabilitiesById[cId])
            .map(c => ([c.level1, c.level2, c.level3, c.level4, c.level5]))
            .flatten()
            .compact()
            .uniq()
            .map(cId => nodesById[cId])
            .compact()
            .value();

        vm.treeData = _.filter(nodeData, n => ! n.parentId);
        vm.expandedNodes = expandedNodes;
        vm.primaryCapabilityIds = primaryCapabilityIds;
        vm.explicitCapabilityIds = explicitCapabilityIds;
        vm.capabilitiesForApp = capabilitiesForApp;



    });


    vm.isPrimary = (cId) => _.contains(vm.primaryCapabilityIds, cId);


    // ---- modal


}

controller.$inject = [
    '$stateParams', '$q', 'CapabilityStore', 'AppCapabilityStore'
];


export default {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};