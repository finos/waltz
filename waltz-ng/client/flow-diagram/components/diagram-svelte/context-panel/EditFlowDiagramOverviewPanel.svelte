<script>
    import {createEventDispatcher} from "svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {description, name} from "../store/edit-flow-diagram-panel-store";
    import {flowDiagramStore} from "../../../../svelte-stores/flow-diagram-store";
    import {diagram} from "../store/diagram";

    export let flowDiagram;

    let invalid = true;
    let namePromise;
    let descriptionPromise;

    const dispatch = createEventDispatcher();

    function cancel() {
        $name = flowDiagram.name;
        $description = flowDiagram.description;
        dispatch("cancel");
    }

    $: $name = flowDiagram.name;
    $: $description = flowDiagram.description;

    $: noChange = $name === flowDiagram.name && $description === flowDiagram.description;

    function saveDiagram(){
        console.log({name: $name, desc: $description})

        if($name !== flowDiagram.name){
            const namePromise = flowDiagramStore.updateName(flowDiagram.id, {newName: $name});
        }
        if($description !== flowDiagram.description) {
            const descriptionPromise = flowDiagramStore.updateDescription(flowDiagram.id, {newDescription: $description});
        }

        $diagram.name = $name;
        $diagram.description = $description;
        cancel();
        return namePromise;
    }

</script>



<form autocomplete="off"
      on:submit|preventDefault={saveDiagram}>
    <div class="form-group">
        <label for="name">
            Name:
            <span style="color: darkred">*</span>
        </label>
        <input class="form-control"
               id="name"
               bind:value={$name}/>
    </div>
    <p class="text-muted">Name for this diagram</p>

    <div class="form-group">
        <label for="description">
            Description:
        </label>
        <textarea class="form-control"
                  rows="4"
                  id="description"
                  bind:value={$description}/>
    </div>
    <p class="text-muted">Description for this diagram</p>

    <button class="btn btn-success"
            type="submit"
            disabled={_.isNil($name) || noChange || (namePromise || descriptionPromise)}>
        Save
    </button>
    <button class="btn btn-default"
            on:click|preventDefault={cancel}>
        Cancel
    </button>
</form>

{#if namePromise}
    {#await namePromise}
        Saving...
    {:then r}
        Saved!
    {:catch e}
        <div class="alert alert-warning">
            Failed to save updated name. Reason: {e.error}
            <button class="btn-link"
                    on:click={() => namePromise = null}>
                <Icon name="check"/>
                Okay
            </button>
        </div>
    {/await}
{/if}

{#if descriptionPromise}
    {#await descriptionPromise}
        Saving...
    {:then r}
        Saved!
    {:catch e}
        <div class="alert alert-warning">
            Failed to save updated description. Reason: {e.error}
            <button class="btn-link"
                    on:click={() => descriptionPromise = null}>
                <Icon name="check"/>
                Okay
            </button>
        </div>
    {/await}
{/if}


<style>

</style>