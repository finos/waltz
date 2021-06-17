<script>
    import {createEventDispatcher} from "svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {diagram} from "../store/diagram";
    import {flowDiagramStore} from "../../../../svelte-stores/flow-diagram-store";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";

    export let diagramId;

    const dispatch = createEventDispatcher();

    let savePromise;
    let newName = `Clone of ${$diagram.name}`;

    function cancel() {
        dispatch("cancel");

    }

    let newDiagram;

    function saveClone() {
        savePromise = flowDiagramStore.clone(diagramId, newName);

        savePromise
            .then(r => r.subscribe(d => newDiagram = d.data));

        return savePromise;
    }

</script>


<h4>Cloning: {$diagram.name}</h4>
<form autocomplete="off"
      on:submit|preventDefault={saveClone}>

    {#if _.isNil(newDiagram)}
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
    {:else}
        <div>
            <EntityLink ref={newDiagram}/>
        </div>
        <br>
    {/if}
    <button class="btn btn-success"
            type="submit"
            disabled={_.isNil(newName) || _.isEmpty(newName) || savePromise}>
        Save
    </button>
    <button class="btn btn-default"
            on:click|preventDefault={cancel}>
        Cancel
    </button>
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
</form>



<style>

</style>