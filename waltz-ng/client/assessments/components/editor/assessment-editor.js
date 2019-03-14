/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

import {initialiseData} from "../../../common";
import template from "./assessment-editor.html";


const bindings = {
    onSave: "<",
    assessment: "<",
    onClose: "<",
    onRemove: "<"
};


const initialState = {

};


function controller(notification) {
    const vm = initialiseData(this, initialState);

    vm.onSaveRating = (ratingId) => {
        const comments = vm.assessment.rating ? vm.assessment.rating.description : "";
        return vm.onSave(ratingId, comments, vm.assessment);
    };

    vm.onSaveDescription = (commentChange) => {
        const rating = vm.assessment.rating;
        if (rating){
            return vm.onSave(rating.ratingId, commentChange.newVal, vm.assessment);
        } else {
            notification.warning("Please create a rating before you add any description");
        }
    }

}


controller.$inject = [
    "Notification"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAssessmentEditor"
};
