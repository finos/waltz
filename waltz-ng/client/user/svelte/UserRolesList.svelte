<script>
    import {selectedUser, userRoles, rolesChanged, activeMode, Modes} from "./user-management-store";
    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import _ from "lodash";
    import {roleStore} from "../../svelte-stores/role-store";
    import NoData from "../../common/svelte/NoData.svelte";
    import {termSearch} from "../../common";
    import {userStore} from "../../svelte-stores/user-store";
    import toasts from "../../svelte-stores/toast-store";
    import Icon from "../../common/svelte/Icon.svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";

    let qry = "";
    let comment = null;
    let expandedReadOnly = false;

    $: rolesCall = roleStore.findAll();
    $: roles = $rolesCall?.data;

    let userSelectableRoles;
    let readOnlyRoles;

    $: [userSelectableRoles, readOnlyRoles] = _.partition(roles, r => r.userSelectable);

    $: userReadOnlyRoles = _.filter(readOnlyRoles, d => _.includes($selectedUser?.roles, d.key));
    $: userSelectableRoleKeys = _.map(userSelectableRoles, d => d.key);

    $: displayedRoles = _.isEmpty(qry)
        ? userSelectableRoles
        : termSearch(userSelectableRoles, qry, ["name", "key", "value", "description"]);

    function updateUserRoles() {
        const updatePromise = userStore
            .updateRoles($selectedUser.userName, _.values($userRoles), comment);

        Promise.resolve(updatePromise)
            .then(r => {
                $activeMode = Modes.LIST;
                toasts.success("Successfully updated roles for " + $selectedUser.userName);
            })
            .catch(e => toasts.error("Unable to update roles: " + e.error))
    }

    function selectRole(role) {
        let roleExists = _.includes($userRoles, role.key);

        $userRoles = roleExists
            ? _.reject($userRoles, d => d === role.key)
            : _.concat($userRoles, role.key);
    }

    function cancel() {
        $activeMode = Modes.LIST;
        $userRoles = [];
        $selectedUser = null;

        if ($rolesChanged) {
            toasts.warning("Changes to roles for " + $selectedUser.userName + " discarded")
        }
    }

    function addAllRoles() {
        const rolesToAdd = _.without(userSelectableRoleKeys, $userRoles);
        $userRoles = _.concat($userRoles, rolesToAdd);
    }

    function removeAllRoles() {
        $userRoles = _.reject($userRoles, d => _.includes(userSelectableRoleKeys, d));
    }

    function hasRole(userRoles, role) {
        return _.includes(userRoles, role);
    }

</script>

{#if _.isEmpty($selectedUser)}
    <NoData>There is no selected user</NoData>
{:else }
    <h4>{$selectedUser.userName}</h4>
    <p>
        You can search for a role to edit below, delete this user or
        <button class="btn btn-skinny"
                title="Save or discard changes to roles before editing password"
                disabled={$rolesChanged}
                on:click={() => $activeMode = Modes.PASSWORD}>
            <Icon name="key"/> change the user's password
        </button>
    </p>
    <SearchInput bind:value={qry}
                 placeholder="Search for a role..."/>
    <br>
    <div class:waltz-scroll-region-350={_.size(displayedRoles) > 10}>
        <table class="table table-condensed small table-hover">
            <colgroup>
                <col width="10%">
                <col width="30%">
                <col width="20%">
                <col width="30%">
                <col width="10%">
            </colgroup>
            <thead>
            <tr>
                <th>
                    <button class="btn btn-skinny"
                            on:click={() => addAllRoles()}>
                        <Icon name="plus"/>
                    </button>
                    /
                    <button class="btn btn-skinny"
                             on:click={() => removeAllRoles()}>
                        <Icon name="minus"/>
                    </button>

                </th>
                <th>Role</th>
                <th>Key</th>
                <th>Description</th>
                <th>Custom Role</th>
            </tr>
            </thead>
            <tbody>
            {#each _.orderBy(displayedRoles, d => _.toLower(d.name)) as role}
                <tr>
                    <td>
                        <input type="checkbox"
                               on:change={() => selectRole(role)}
                               checked={hasRole($userRoles, role.key)}>
                    </td>
                    <td>
                        <ViewLink state="main.role.view"
                                  ctx={{id: role.id}}>
                            {role.name}
                        </ViewLink>
                    </td>
                    <td>{role.key}</td>
                    <td>{role.description || ""}</td>
                    <td>
                        <input type="checkbox"
                               disabled={true}
                               checked={role.isCustom}>
                    </td>
                </tr>
            {:else}
                <tr>
                    <td colspan="4">
                        <NoData>There are no roles to display</NoData>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>

    {#if !_.isEmpty(userReadOnlyRoles)}
        <div style="padding-top: 1em">
            <NoData type="info">
                This user has {_.size(userReadOnlyRoles)} roles which are read only and cannot be edited through this view.
                {#if !expandedReadOnly}
                    <span>
                        <button class="btn btn-skinny"
                                on:click={() => expandedReadOnly = true}>
                            Show additional roles <Icon name="caret-down"/>
                        </button>
                    </span>
                {:else }
                    <span>
                        <button class="btn btn-skinny"
                                on:click={() => expandedReadOnly = false}>
                            Hide additional roles <Icon name="caret-up"/>
                        </button>
                    </span>
                    <div class:waltz-scroll-region-250={_.size(userReadOnlyRoles) > 10}>
                        <ul>
                            {#each userReadOnlyRoles as role}
                                <li>
                                    {role.name}
                                </li>
                            {/each}
                        </ul>
                    </div>
                {/if}
            </NoData>
        </div>
    {/if}

    <br>

    <input class="form-control"
           id="comment"
           maxlength="255"
           placeholder="Add a comment to the user's changelog"
           bind:value={comment}/>
    <div class="help-block small">
        <Icon name="info-circle"/> Add a comment to the change log for this change.
    </div>

    <span>
        <button class="btn btn-success"
                disabled={!$rolesChanged}
                on:click={() => updateUserRoles()}>
            Save Updates
        </button>
        <button class="btn btn-danger"
                on:click={() => $activeMode = Modes.DELETE}>
            Delete User
        </button>
        <button class="btn btn-skinny"
                on:click={() => cancel()}>
            Cancel
        </button>
    </span>
{/if}
