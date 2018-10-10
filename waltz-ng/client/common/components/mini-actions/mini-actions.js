import template from "./mini-actions.html";
import {initialiseData} from "../../index";

const bindings = {
    actions: "<",
    ctx: "<"
};


const initialState = {

};


/**
 * Renders a mini action bar, where each action looks like:
 * ```
 * {
 *     type: 'action' | 'link',
 *     name: <string>,
 *     icon: <string>,
 *     predicate:  fn(ctx) => bool,
 *     execute: fn(ctx) => ...        // if type == 'action'
 *     state: <string> => ...         // if type == 'link'
 *     stateParams: <string> => ...   // if type == 'link'
 * }
 * ```
 * @type {{actions: string, ctx: string}}
 */

function controller() {

    const vm = initialiseData(this, initialState);

    function canShow(action) {
        if (action.predicate) {
            return action.predicate(vm.ctx);
        } else {
            return true;
        }
    }

    vm.$onChanges = () => {
        vm.availableActions = _.filter(vm.actions, a => canShow(a));
    };
}


controller.$inject = [];


const component = {
    controller,
    bindings,
    template
};


export default {
    id: "waltzMiniActions",
    component
}