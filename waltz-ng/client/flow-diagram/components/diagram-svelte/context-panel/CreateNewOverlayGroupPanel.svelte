<script>
    import {flowDiagramOverlayGroupStore} from "../../../../svelte-stores/flow-diagram-overlay-group-store";
    import {createEventDispatcher} from "svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {toUpperSnakeCase} from "../../../../common/string-utils";
    import {description, name} from "../store/create-overlay-panel-store";

    export let diagramId;

    let invalid = true;
    let savePromise;

    const dispatch = createEventDispatcher();

    function cancel() {
        $name = null;
        $description = null;
        dispatch("cancel");
    }


    function saveOverlayGroup(){
        const savePromise = flowDiagramOverlayGroupStore.createGroup(
            {
                name: $name,
                description: $description,
                diagramId: diagramId,
                externalId: toUpperSnakeCase($name)
            });
        cancel();
        return savePromise;
    }

</script>



<form autocomplete="off"
      on:submit|preventDefault={saveOverlayGroup}>
    <div class="form-group">
        <label for="name">
            Name:
            <span style="color: darkred">*</span>
        </label>
        <input class="form-control"
               id="name"
               bind:value={$name}/>
    </div>
    <p class="text-muted">Set a name for the new overlay group</p>

    <div class="form-group">
        <label for="description">
            Description:
        </label>
        <input class="form-control"
               id="description"
               bind:value={$description}/>
    </div>
    <p class="text-muted">Enter a description for this group of overlays</p>

    <button class="btn btn-success"
            type="submit"
            disabled={_.isNil($name) || savePromise}>
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
            Failed to create overlay group. Reason: {e.error}
            <button class="btn-link"
                    on:click={() => savePromise = null}>
                <Icon name="check"/>
                Okay
            </button>
        </div>
    {/await}
{/if}


<style>

</style>