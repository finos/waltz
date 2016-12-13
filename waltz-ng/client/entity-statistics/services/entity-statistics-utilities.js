

/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

function utils(appGroupStore, capabilityStore, orgUnitStore, processStore) {

    const findAllForKind = (kind) => {
        switch (kind) {
            case 'APP_GROUP':
                return appGroupStore
                    .findMyGroupSubscriptions()
                    .then(gs => _.map(gs, 'appGroup'));
            case 'ORG_UNIT':
                return orgUnitStore.findAll();
            case 'CAPABILITY':
                return capabilityStore.findAll();
            case 'PROCESS':
                return processStore.findAll();
            default :
                throw 'Cannot create hierarchy for kind: '+kind;
        }
    };

    return {
        findAllForKind
    };
}


utils.$inject = [
    'AppGroupStore',
    'CapabilityStore',
    'OrgUnitStore',
    'ProcessStore'
];


export default utils;