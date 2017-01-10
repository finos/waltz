/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";


const initialState = {
    changeInitiatives: [],
    selectedChangeInitiative: null,
};


function setup(groupDetail) {
    const { applications, members, appGroup } = groupDetail;

    const owners = _.filter(members, m => m.role === 'OWNER' );
    const viewers = _.filter(members, m => m.role === 'VIEWER' );

    return {
        owners,
        viewers,
        applications,
        appGroup
    };
}

function handleError(e) {
    alert(e.data.message);
}


function controller($q,
                    $scope,
                    $stateParams,
                    appGroupStore,
                    appStore,
                    changeInitiativeStore,
                    logicalFlowStore,
                    notification) {

    const { id }  = $stateParams;
    const vm = Object.assign(this, initialState);



    appGroupStore.getById(id)
        .then(groupDetail => setup(groupDetail))
        .then(data => Object.assign(vm, data));


    vm.addToGroup = (app) => {
        appGroupStore.addApplication(id, app.id)
            .then(apps => vm.applications = apps, e => handleError(e))
            .then(() => notification.success('Added: ' + app.name));
    };


    vm.removeFromGroup = (app) => {
        appGroupStore.removeApplication(id, app.id)
            .then(apps => vm.applications = apps, e => handleError(e))
            .then(() => notification.warning('Removed: ' + app.name));
    };


    vm.isAppInGroup = (app) => {
        return _.some(vm.applications, a => app.id === a.id);
    };


    vm.promoteToOwner = (member) => {
        appGroupStore.addOwner(member.groupId, member.userId)
            .then(members => {
                vm.owners = _.filter(members, m => m.role === 'OWNER');
                vm.viewers = _.filter(members, m => m.role === 'VIEWER');
            })
            .then(() => notification.success('User: ' + member.userId + ' is now an owner of the group'));

    };


    vm.updateGroupOverview = () => {
        appGroupStore.updateGroupOverview(id, vm.appGroup)
            .then(() => notification.success('Group details updated'));
    };


    vm.focusOnApp = (app) => {
        const focusApp = {};

        appStore.getById(app.id)
            .then(fullApp => {
                focusApp.app = fullApp;
                const promises = [
                    appStore.findRelatedById(fullApp.id),
                    logicalFlowStore.findByEntityReference('APPLICATION', fullApp.id),
                    appStore.findBySelector({ entityReference: { id: fullApp.organisationalUnitId, kind: 'ORG_UNIT'}, scope: 'EXACT'})
                ];
                return $q.all(promises);
            })
            .then(([ related, flows, unitMembers]) => {
                focusApp.related = _.flatten(_.values(related));
                focusApp.unitMembers = _.reject(unitMembers, m => m.id === app.id);
                focusApp.upstream = _.chain(flows)
                    .map(f => f.source)
                    .uniqBy(source => source.id)
                    .reject(source => source.id === app.id)
                    .value();
                focusApp.downstream = _.chain(flows)
                    .map(f => f.target)
                    .uniqBy(target => target.id)
                    .reject(target => target.id === app.id)
                    .value();
            })
            .then(() => vm.focusApp = focusApp);
    };


    // add app via search
    vm.searchedApp = {};

    $scope.$watch('ctrl.searchedApp.app', (app) => {
        if (! _.isObject(app)) return;
        vm.addToGroup(app);
        vm.focusOnApp(app);
    }, true);


    $scope.$watch(
        'ctrl.selectedChangeInitiative',
        (changeInitiative) => {
            if (!changeInitiative) return;

            appGroupStore
                .addChangeInitiative(id, changeInitiative.id)
                .then(cis => vm.changeInitiatives = cis)
                .then(() => notification.success('Associated Change Initiative: ' + changeInitiative.name));

        });

    vm.removeChangeInitiative = (changeInitiative) => appGroupStore
        .removeChangeInitiative(id, changeInitiative.id)
        .then(cis => vm.changeInitiatives = cis)
        .then(() => notification.warning('Removed Change Initiative: ' + changeInitiative.name));

    changeInitiativeStore
        .findByRef('APP_GROUP', id)
        .then(cis => vm.changeInitiatives = cis);

}

controller.$inject = [
    '$q',
    '$scope',
    '$stateParams',
    'AppGroupStore',
    'ApplicationStore',
    'ChangeInitiativeStore',
    'LogicalFlowStore',
    'Notification'
];


export default {
    template: require('./app-group-edit.html'),
    controller,
    controllerAs: 'ctrl'
};
