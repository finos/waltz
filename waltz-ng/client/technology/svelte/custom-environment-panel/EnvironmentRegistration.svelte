<script>

    import {customEnvironmentStore} from "../../../svelte-stores/custom-environment-store";

    import {panelMode, PanelModes} from "./editingCustomEnvironmentState";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {refToString} from "../../../common/entity-utils";

    export let onCancel;
    export let primaryEntityRef;

    $: newEnvironment = {
        name: null,
        description: null,
        owningEntity: primaryEntityRef,
        externalId: null,
        group: null
    };

    let invalid = true;
    let savePromise;


    let workingCopy = Object.assign({}, newEnvironment);


    function saveEnvironment() {

        const unformattedExtId = `${refToString(primaryEntityRef)}_${workingCopy.name.toUpperCase()}`;
        const externalId = _.toUpper(_.replace(unformattedExtId, /\s+/g, "_"));

        const env = {
            name: workingCopy.name,
            description: workingCopy.description,
            owningEntity: primaryEntityRef,
            externalId: externalId,
            groupName: workingCopy.group
        }

        savePromise = customEnvironmentStore
            .create(env)
            .then(() => panelMode.set(PanelModes.VIEW));
    }

    function cancel() {
        onCancel();
    }

    $: invalid = _.isNil(workingCopy.name);

</script>


<h4>Register new custom environment:</h4>

<form autocomplete="off"
      on:submit|preventDefault={saveEnvironment}>

    {#if !workingCopy.id}

        <div class="form-group">
            <label for="group">Group:</label>
            <input class="form-control"
                   id="group"
                   placeholder="Leave blank for default"
                   bind:value={workingCopy.group}/>
        </div>
        <p class="text-muted">You can use this field to group environments together</p>

        <div class="form-group">
            <label for="name">
                Name:
                <span style="color: darkred">*</span>
            </label>
            <input class="form-control"
                   id="name"
                   bind:value={workingCopy.name}/>
        </div>
        <p class="text-muted">Names must be unique</p>

        <div class="form-group">
            <label for="description">Description:</label>
            <textarea class="form-control"
                      id="description"
                      bind:value={workingCopy.description}/>
        </div>
        <p class="text-muted">Additional notes about this environment</p>

    {/if}

    <button class="btn btn-success"
            type="submit"
            disabled={invalid || savePromise}>
        Save
    </button>
    <button class="btn btn-default"
            on:click|preventDefault={cancel}>
        Cancel
    </button>
</form>

{#if savePromise}
    {#await savePromise}
        Saving...
    {:then r}
        Saved!
    {:catch e}
        <div class="alert alert-warning">
            Failed to save custom environment. Reason: {e.error}
            <button class="btn-link"
                    on:click={() => savePromise = null}>
                <Icon name="check"/>
                Okay
            </button>
        </div>
    {/await}
{/if}
