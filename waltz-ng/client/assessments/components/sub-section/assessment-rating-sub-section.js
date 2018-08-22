import {initialiseData} from "../../../common";

import template from "./assessment-rating-sub-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    visibility: {
        editor: false,
        editBtn: false
    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.AssessmentDefinitionStore.findByKind, [ vm.parentEntityRef.kind ])
            .then(r => {
                const hasDefinitions = (r.data || []).length > 0;
                const allReadOnly = _.every(r.data, d => d.isReadOnly);
                vm.visibility.editBtn = hasDefinitions && ! allReadOnly;
            });
    };

}


controller.$inject = ['ServiceBroker'];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAssessmentRatingSubSection"
};
