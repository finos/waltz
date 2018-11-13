import template from "./measurable-change-control.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";
import {findHighestSeverity, severityToBootstrapBtnClass} from "../../../common/severity-utils";

const modes = {
    MENU: "MENU",
    OPERATION: "OPERATION",
};


const bindings = {
    measurable: "<",
    changeDomain: "<",
};


const initialState = {
    modes: modes,
    mode: modes.MENU,
    selectedOperation: null,
    preview: null,
    command: null
};


function controller(notification,
                    serviceBroker,
                    userService) {

    const vm = initialiseData(this, initialState);

    function mkUpdCmd(newValue) {
        return {
            changeType: vm.selectedOperation.code,
            changeDomain: toEntityRef(vm.changeDomain),
            a: toEntityRef(vm.measurable),
            newValue: newValue+"",
            createdBy: vm.userName
        };
    }


    const updateMenu = {
        name: "Update",
        description: `These are operations modify an existing taxonomy element.  Care must be taken 
                to prevent inadvertently altering the <em>meaning</em> of nodes.  The operations 
                will not result in data loss.`,
        options: [
            {
                name: "Name",
                code: "UPDATE_NAME",
                title: "Update name",
                description: `The name of the taxonomy item may be changed, however care should be 
                    taken to prevent inadvertently altering the <em>meaning</em> of the item`,
                icon: "edit",
            }, {
                name: "Description",
                code: "UPDATE_DESCRIPTION",
                icon: "edit"
            }, {
                name: "Concrete",
                code: "UPDATE_CONCRETENESS",
                title: "Update Concrete Flag",
                description: `The concrete flag is used to determine whether applications may
                    use this taxonomy item to describe themselves via ratings.  Typically
                    higher level <em>grouping</em> items are non-concrete as they are not
                    specific enough to accurately describe the portfolio.`,
                icon: "edit",
                onShow: () => {
                    vm.command = mkUpdCmd(!vm.measurable.concrete);
                    return serviceBroker
                        .execute(CORE_API.TaxonomyManagementStore.preview, [ vm.command ])
                        .then(r => {
                            vm.preview = r.data;
                            vm.submitButtonClass = severityToBootstrapBtnClass(
                                findHighestSeverity(
                                    _.map(vm.preview.impacts, "severity")));
                        })
                }
            }, {
                name: "External Id",
                code: "UPDATE_EXTERNAL_ID",
                icon: "edit"
            }, {
                name: "Move",
                code: "MOVE",
                icon: "arrows"
            }
        ]
    };

    const creationMenu = {
        name: "Create",
        description: `These operations introduce new elements in the taxonomy. They will 
                <strong>not</strong> result in data loss.`,
        color: "#0b8829",
        options: [
            {
                name: "Add Child",
                code: "ADD",
                icon: "plus-circle"
            }, {
                name: "Clone",
                code: "CLONE",
                icon: "clone"
            }
        ]
    };

    const destructiveMenu = {
        name: "Destructive",
        description: `These operations <strong>will</strong> potentially result in data loss and 
                should be used with care`,
        color: "#b40400",
        options: [
            {
                name: "Merge",
                code: "MERGE",
                icon: "code-fork"
            }, {
                name: "Deprecate",
                code: "DEPRECATE",
                icon: "exclamation-triangle"
            }, {
                name: "Destroy",
                code: "REMOVE",
                icon: "trash"
            }
        ]
    };

    vm.menus = [
        updateMenu,
        creationMenu,
        destructiveMenu
    ];

    // --- boot

    vm.$onInit = () => {
        userService
            .whoami()
            .then(u => vm.userName = u.userName);
    };

    vm.$onChanges = (c) => {
        if (c.measurable) {
            vm.onDismiss();
        }
    };


    // --- interact

    vm.toTemplateName = (op) => `wmcc/${op.code}.html`;

    vm.onDismiss = () => {
        vm.mode = modes.MENU;
        vm.command = null;
        vm.preview = null;
    };

    vm.onSelectOperation = (op) => {
        vm.mode = modes.OPERATION;
        vm.selectedOperation = op;
        return _.isFunction(op.onShow)
            ? op.onShow()
            : Promise.resolve();
    };

    vm.onSubmitChange = () => {
        serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.submitPendingChange,
                [ vm.command ])
            .then(r => notification.info("Change submitted"))
    };

}


controller.$inject = [
    "Notification",
    "ServiceBroker",
    "UserService"
];


const component = {
    bindings,
    controller,
    template
};


export default {
    id: "waltzMeasurableChangeControl",
    component
}
