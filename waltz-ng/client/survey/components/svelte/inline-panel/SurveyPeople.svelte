<script>
    import _ from "lodash";

    import Toasts from "../../../../svelte-stores/toast-store";
    import {surveyInstanceStore} from "../../../../svelte-stores/survey-instance-store";
    import {displayError} from "../../../../common/error-utils";
    import PersonList from "../../../../common/svelte/PersonList.svelte";


    export let id;

    let ownersCall = null;
    let recipientsCall = null;


    function reload() {
        ownersCall = surveyInstanceStore.findOwners(id, true)
        recipientsCall = surveyInstanceStore.findRecipients(id, true)
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


    $: owners = _.sortBy($ownersCall?.data, d => d.name);
    $: recipients = _.sortBy($recipientsCall?.data, d => d.name);

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
        <td>Approvers</td>
        <td>
            <PersonList people={owners}
                        onAdd={onAddOwner}
                        onRemove={onRemoveOwner}
                        canAdd={permissions?.isMetaEdit}
                        canRemove={permissions?.isMetaEdit}
                        canRemoveSelf={false}/>
        </td>
    </tr>
    </tbody>
</table>


