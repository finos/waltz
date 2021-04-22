<script>

    import {customEnvironmentStore} from "../../../svelte-stores/custom-environment-store";

    import {mode, Modes, selectedEnvironment} from "./editingCustomEnvironmentState";
    import Icon from "../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {refToString} from "../../../common/entity-utils";

    export let onCancel;
    export let primaryEntityRef;
    let invalid = true;
    let savePromise;


    let workingCopy = Object.assign({}, $selectedEnvironment);


    function saveEnvironment() {

        console.log({primaryEntityRef, workingCopy});

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
            .create(env);

        mode.set(Modes.LIST);
    }

    function cancel() {
        onCancel();
    }

    function fieldChanged(d){
        d.name === $selectedEnvironment.name && d.description === $selectedEnvironment.description && d.group === $selectedEnvironment.group
    }

    $: invalid = (workingCopy.id)
        ? ! fieldChanged(workingCopy)
        : _.isNil(workingCopy.name);

</script>


<h3>Register new custom environment:</h3>

<form autocomplete="off"
      on:submit|preventDefault={saveEnvironment}>

    {#if !workingCopy.id}

        <div class="form-group">
            <label for="name">Group:</label>
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
            <label for="name">Description:</label>
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
            Failed to save custom environment. Reason: {e.data.message}
            <button class="btn-link"
                    on:click={() => savePromise = null}>
                <Icon name="check"/>
                Okay
            </button>
        </div>
    {/await}
{/if}
