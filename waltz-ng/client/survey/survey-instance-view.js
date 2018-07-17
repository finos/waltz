/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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
import {initialiseData} from "../common";
import {timeFormat} from "d3-time-format";
import template from './survey-instance-view.html';

const initialState = {
    surveyInstance: {},
    surveyRun: {},
    recipients: [],
    addingRecipient: false,
    newRecipient: null
};


function mkRecipientIdAndEntity(instanceRecipientId, person) {
    return {
        instanceRecipientId,
        person: {
            kind: 'PERSON',
            id: person.id,
            name: person.displayName
        }
    }
}


function mkUpdateRecipientCommand(instanceRecipientId, person, surveyInstanceId) {
    return {
        instanceRecipientId,
        surveyInstanceId,
        personId: person.id,
    }
}


function mkCreateRecipientCommand(surveyInstanceId, person) {
    return {
        surveyInstanceId,
        personId: person.id
    };
}


function controller($stateParams,
                    notification,
                    surveyInstanceStore,
                    surveyRunStore) {

    const vm = initialiseData(this, initialState);
    const id = $stateParams.id;

    const loadInstanceAndRun = () => {
        surveyInstanceStore
            .getById(id)
            .then(surveyInstance => {
                vm.surveyInstance = surveyInstance;
                return surveyRunStore
                    .getById(surveyInstance.surveyRunId)
            })
            .then(sr => vm.surveyRun = sr);
    };

    const loadRecipients = () => {
        return surveyInstanceStore
            .findRecipients(id)
            .then(sirs => vm.recipients = _.map(sirs, sir => mkRecipientIdAndEntity(sir.id, sir.person)));
    };


    vm.editRecipient = (instanceRecipientId, data) => {
        const cmd = mkUpdateRecipientCommand(instanceRecipientId, data.newVal, vm.surveyInstance.id);
        surveyInstanceStore
            .updateRecipient(vm.surveyInstance.id, cmd)
            .then(result => {
                if(result) {
                    loadRecipients();
                    notification.success("Updated survey recipient");
                }
            });
    };

    vm.startNewRecipient = () => {
        vm.addingRecipient = true;
    };

    vm.cancelNewRecipient = () => {
        vm.addingRecipient = false;
    };

    vm.addRecipient = () => {

        surveyInstanceStore
            .addRecipient(vm.surveyInstance.id, mkCreateRecipientCommand(vm.surveyInstance.id, vm.newRecipient))
            .then(result => {
                if(result) {
                    loadRecipients();
                    notification.success("Recipient added");
                    vm.addingRecipient = false;
                }
            });
    };

    vm.selectNewRecipient = (entity) => {
        vm.newRecipient = entity;
    };

    vm.removeRecipient = (recipient) => {
        surveyInstanceStore
            .deleteRecipient(vm.surveyInstance.id, recipient.instanceRecipientId)
            .then(result => {
                if(result) {
                    loadRecipients();
                    notification.success("Recipient deleted");
                }
            });
    };

    vm.updateDueDate = (instanceId, change) => {
        if (!change.newVal) {
            notification.error('Due date cannot be blank');
        } else {
            surveyInstanceStore
                .updateDueDate(
                    instanceId,
                    {newDateVal: timeFormat('%Y-%m-%d')(change.newVal)})
                .then(() => {
                    notification.success('Survey instance due date updated successfully');
                    loadInstanceAndRun();
                },
                    r => notification.error('Failed to update survey instance due date')
                );
        }
    };

    vm.markAsWithdrawn = () => {
        if (confirm('Are you sure you want to withdraw this survey instance? ')) {
            surveyInstanceStore.updateStatus(
                vm.surveyInstance.id,
                {newStatus: 'WITHDRAWN'}
            )
                .then(() => {
                    notification.success('Survey instance withdrawn');
                    loadInstanceAndRun();
                });
        }
    };

    vm.reject = () => {
        const reason = prompt('Are you sure you want reject this survey? Please enter a reason below (mandatory):');

        if (reason) {
            surveyInstanceStore.updateStatus(
                vm.surveyInstance.id,
                {
                    newStatus: 'REJECTED',
                    reason
                }
                )
                .then(() => {
                    notification.success('Survey response rejected');
                    $state.reload();
                });
        }
    };

    loadInstanceAndRun();
    loadRecipients();
}


controller.$inject = [
    '$stateParams',
    'Notification',
    'SurveyInstanceStore',
    'SurveyRunStore'
];


const view = {
    controller,
    controllerAs: 'ctrl',
    template
};


export default view;