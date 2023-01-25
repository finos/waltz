<script>
    import _ from "lodash";

    import Toasts from "../../../../svelte-stores/toast-store";
    import {surveyInstanceStore} from "../../../../svelte-stores/survey-instance-store";
    import {displayError} from "../../../../common/error-utils";
    import PersonList from "../../../../common/svelte/PersonList.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";


    export let id;
    export let owningRole = null;

    let ownersCall = null;
    let recipientsCall = null;
    let viewGroupApproversList = false;
    let groupApproversCall = null;

    function reload() {
        ownersCall = surveyInstanceStore.findOwners(id, true);
        recipientsCall = surveyInstanceStore.findRecipients(id, true);
        groupApproversCall = surveyInstanceStore.findGroupApprovers(id, true);
    }


    function onAddRecipient(person) {
        return surveyInstanceStore
            .addRecipient(id, person.id)
            .then(() => Toasts.success(`Added recipient: ${person.name}`))
            .then(reload)
            .catch(e => displayError("Failed to add recipient", e));
    }


    function onAddOwner(person) {
        return surveyInstanceStore
            .addOwner(id, person.id)
            .then(() => Toasts.success(`Added owner: ${person.name}`))
            .then(reload)
            .catch(e => displayError("Failed to add owner", e));
    }

    function onRemoveOwner(person) {
        return surveyInstanceStore
            .deleteOwner(id, person.id)
            .then(() => Toasts.success(`Removed owner: ${person.name}`))
            .then(reload)
            .catch(e => displayError("Failed to remove owner", e));
    }

    function onRemoveRecipient(person) {
        return surveyInstanceStore
            .deleteRecipient(id, person.id)
            .then(() => Toasts.success(`Removed recipient: ${person.name}`))
            .then(reload)
            .catch(e => displayError("Failed to remove recipient", e));
    }


    $: owners = _.sortBy($ownersCall?.data, d => _.toLower(d.name));
    $: recipients = _.sortBy($recipientsCall?.data, d => _.toLower(d.name));
    $: groupApprovers = _.sortBy($groupApproversCall?.data, d => _.toLower(d.name));

    $: permissionsCall = id && surveyInstanceStore.getPermissions(id);
    $: permissions = $permissionsCall.data;

    $: id && reload();
</script>


<table class="table table-condensed small">
    <colgroup>
        <col width="50%">
        <col width="50%">
    </colgroup>
    <tbody>
    <tr style="vertical-align: top">
        <td>Recipients</td>
        <td>
            <PersonList people={recipients}
                        onAdd={onAddRecipient}
                        onRemove={onRemoveRecipient}
                        canAdd={permissions?.isMetaEdit}
                        canRemove={permissions?.isMetaEdit}
                        canRemoveSelf={false}/>
        </td>
    </tr>
    <tr style="vertical-align: top">
        <td>Individual Approvers</td>
        <td>

            <PersonList people={owners}
                        onAdd={onAddOwner}
                        onRemove={onRemoveOwner}
                        canAdd={permissions?.isMetaEdit}
                        canRemove={permissions?.isMetaEdit}
                        canRemoveSelf={false}/>
        </td>
    </tr>
    {#if !_.isNil(owningRole)}
        <tr style="vertical-align: top">
            <td>Group Approvers</td>
            <td>
                <Icon name="group"/>
                {owningRole}
                {#if viewGroupApproversList}
                    <div style="margin-top: 1em"
                         class:waltz-scroll-region-250={_.size(groupApprovers) > 14}>
                        <PersonList people={groupApprovers}
                                    canAdd={false}
                                    canRemove={false}
                                    canRemoveSelf={false}/>
                    </div>
                    <button class="btn btn-xs btn-skinny"
                            on:click={() => viewGroupApproversList = false}>
                        Hide group members
                    </button>
                {:else}
                    <button class="btn btn-xs btn-skinny"
                            on:click={() => viewGroupApproversList = true}>
                        View group members
                    </button>
                {/if}
            </td>
        </tr>
    {/if}
    {#if !permissions?.isMetaEdit}
        <tr>
            <td colspan="2">
                <span>
                    <Icon name="info-circle"/>
                    You do not have permissions to edit the recipients or owners of this survey.
                    To make changes please contact:
                </span>
                <ul class="list-inline" style="display: inline-block">
                    {#each owners as owner}
                        <li>
                            <a href="mailto:{owner.email}">
                                {owner.email}
                                <Icon name="envelope-o"/>
                            </a>
                        </li>
                    {/each}
                </ul>
            </td>
        </tr>
    {/if}
    </tbody>
</table>
