<script>
    import {createEventDispatcher} from "svelte";
    import {processor, store} from "../diagram-model-store";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import AddAnnotationSubPanel from "./AddAnnotationSubPanel.svelte";
    import {toGraphId} from "../../../flow-diagram-utils";
    import model from "../store/model";

    export let selected;
    const dispatch = createEventDispatcher();

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
    };

    let activeMode = Modes.VIEW;

    function cancel() {
        dispatch("cancel");
    }

    function removeAnnotation() {
        model.removeAnnotation( {id: toGraphId(selected), data: selected});
    }

    const nodeKinds = ["APPLICATION", "ACTOR"];


    $: isNode = _.includes(nodeKinds, selected.entityReference.kind);
    $: selectedGraphId = toGraphId(selected.entityReference);

    $: owningEntity = (isNode)
        ? _.find($model.nodes, d => d.id === selectedGraphId)
        : _.find($model.flows, d => d.id === selectedGraphId)

    $: owningEntityName = (isNode)
        ? owningEntity.data.name
        : owningEntity.data.source.name + " -> " + owningEntity.data.target.name;

    $: console.log("ap", {s: selected, model: $model, owningEntity, owningEntityName})

</script>

<div>
    <strong>
        Annotation linked to <EntityLink ref={selected.entityReference}><span>{owningEntityName}</span></EntityLink>
    </strong>
</div>


{#if activeMode === Modes.VIEW}
<div>
    <span>{selected.note}</span>
</div>
<span>
    <button class="btn btn-link"
            on:click={() => cancel()}>
        Cancel
    </button>
    |
    <button class="btn btn-link"
            on:click={() => activeMode = Modes.EDIT}>
        Edit
    </button>
    |
    <button class="btn btn-link"
            on:click={() => removeAnnotation()}>
        Remove
    </button>
</span>
{:else if activeMode === Modes.EDIT}
    <AddAnnotationSubPanel selected={selected}
                           on:cancel={() => cancel()}/>
{/if}

<style>
</style>