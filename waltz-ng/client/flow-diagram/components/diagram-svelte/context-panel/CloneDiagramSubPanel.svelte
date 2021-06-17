<script>
    import {createEventDispatcher} from "svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {diagram} from "../store/diagram";
    import {flowDiagramStore} from "../../../../svelte-stores/flow-diagram-store";

    export let diagramId;

    const dispatch = createEventDispatcher();

    let savePromise;
    let newName = `Clone of ${$diagram.name}`;

    function cancel() {
        dispatch("cancel");

    }

    function saveClone(){
        flowDiagramStore.clone(diagramId, newName);
        cancel();
    }

</script>


<h4>Cloning: {$diagram.name}</h4>
<form autocomplete="off"
      on:submit|preventDefault={saveClone}>

    <div class="form-group">
        <label for="name">
            Name:
            <span style="color: darkred">*</span>
        </label>
        <input class="form-control"
               id="name"
               bind:value={newName}/>
    </div>
    <p class="text-muted">Name for the cloned diagram</p>

    <button class="btn btn-success"
            type="submit"
            disabled={_.isNil(newName) || _.isEmpty(newName) || savePromise}>
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
            Failed to clone diagram. Reason: {e.error}
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