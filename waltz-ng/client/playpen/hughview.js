import _ from 'lodash';

function controller($stateParams,
                    $q,
                    appGroupStore,
                    capabilityStore,
                    appCapabilityStore,
                    ratingStore) {

    const groupId = $stateParams.groupId;

    const vm = this;

    const groupPromise = appGroupStore.getById(groupId);
    const capabilityPromise = capabilityStore.findAll();

    const promises = [groupPromise, capabilityPromise];


    $q.all(promises).then(([group, allCapabilities]) => {
        vm.group = group;
        vm.allCapabilities = allCapabilities;

        const capabilitiesById = _.indexBy(allCapabilities, 'id');
        const appIds = _.map(group.applications, 'id');

        return { capabilitiesById, appIds };
    }).then(ctx => {

        appCapabilityStore.findApplicationCapabilitiesByAppIds(ctx.appIds)
            .then(acs => _.chain(acs)
                .map('capabilityId')
                .uniq()
                .value())
            .then(explicitCapabilityIds => {
                vm.initiallySelectedIds = explicitCapabilityIds;
                vm.explicitCapabilityIds = explicitCapabilityIds;
                return _.chain(explicitCapabilityIds)
                    .map(cId => ctx.capabilitiesById[cId])
                    .compact()
                    .map(c => ([c.level1, c.level2, c.level3, c.level4, c.level5]))
                    .flatten()
                    .compact()
                    .uniq()
                    .map(cId => ctx.capabilitiesById[cId])
                    .compact()
                    .value()
            })
            .then(capabilities => vm.capabilities = capabilities);

        ratingStore.findByAppIds(ctx.appIds)
            .then(ratings => vm.ratings = ratings);

    });

    vm.group = {};
}

controller.$inject = [
    '$stateParams',
    '$q',
    'AppGroupStore',
    'CapabilityStore',
    'AppCapabilityStore',
    'RatingStore'
];


export default {
    template: require('./hughview.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};