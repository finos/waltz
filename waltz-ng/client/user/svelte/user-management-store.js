import {derived, writable} from "svelte/store";
import _ from "lodash";

export const Modes = {
    LIST: "LIST",
    DETAIL: "DETAIL",
    ADD: "ADD",
    PASSWORD: "PASSWORD",
    DELETE: "DELETE"
}

export const selectedUser = writable(null);
export let activeMode = writable(Modes.LIST);
export let userRoles = writable([]);
export let searchQry = writable("");

export let rolesChanged = derived(
    [selectedUser, userRoles],
    ([$selectedUser, $userRoles]) => {

        if(_.isNull($selectedUser)) {
            return false;
        } else {
            const roleRemoved = _.some($selectedUser.roles, d => !_.includes($userRoles, d));
            const hasNewRoles = _.some($userRoles, d => !_.includes($selectedUser.roles, d));
            return roleRemoved || hasNewRoles;

        }
    });
