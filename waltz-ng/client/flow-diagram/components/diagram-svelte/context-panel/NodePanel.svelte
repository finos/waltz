<script>
    import AddLogicalFlowSubPanel from "./AddLogicalFlowSubPanel.svelte";
    import {Directions} from "./panel-utils";
    import {createEventDispatcher} from "svelte";
    import {processor} from "../diagram-model-store";
    import {toGraphId} from "../../../flow-diagram-utils";
    import {applicationStore} from "../../../../svelte-stores/application-store";
    import {actorStore} from "../../../../svelte-stores/actor-store";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import AddAnnotationSubPanel from "./AddAnnotationSubPanel.svelte";
    import model from "../store/model";

    export let selected;
    const dispatch = createEventDispatcher();

    $: isApp = selected.kind === 'APPLICATION';

    $: nodeCall = (isApp)
        ? applicationStore.getById(selected.id)
        : actorStore.getById(selected.id);

    $: node = $nodeCall.data;

    const Modes = {
        MENU: "MENU",
        ADD_ANNOTATION: "ADD_ANNOTATION",
        ADD_UPSTREAM_FLOW: "ADD_UPSTREAM_FLOW",
        ADD_DOWNSTREAM_FLOW: "ADD_DOWNSTREAM_FLOW",
        REMOVAL: "REMOVAL"
    };

    let activeMode = Modes.MENU;

    function cancel() {
        dispatch("cancel");
    }

    function removeNode() {
        model.removeNode({id: toGraphId(selected), data: selected})
        cancel();
    }

</script>

<div>
    <strong>
        <EntityLink ref={selected}/>
    </strong>
</div>

{#if activeMode === Modes.MENU}
<ul>
    <li>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD_UPSTREAM_FLOW}>
            Add upstream source
        </button>
    </li>
    <li>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD_DOWNSTREAM_FLOW}>
            Add downstream target
        </button>
    </li>
    <li>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD_ANNOTATION}>
            Add annotation
        </button>
    </li>
    <li>
        <button class="btn btn-skinny"
                on:click={() => removeNode()}>
            Remove
        </button>
    </li>
    <li>
        <button class="btn btn-skinny"
                on:click={() => cancel()}>
            Cancel
        </button>
    </li>
</ul>
{:else if activeMode === Modes.ADD_UPSTREAM_FLOW}
    <AddLogicalFlowSubPanel selected={selected}
                            direction={Directions.UPSTREAM}
                            on:cancel={() => activeMode = Modes.MENU}/>
{:else if activeMode === Modes.ADD_DOWNSTREAM_FLOW}
    <AddLogicalFlowSubPanel selected={selected}
                            direction={Directions.DOWNSTREAM}
                            on:cancel={() => activeMode = Modes.MENU}/>
{:else if activeMode === Modes.ADD_ANNOTATION}
    <AddAnnotationSubPanel selected={selected}
                           on:cancel={() => activeMode = Modes.MENU}/>
{/if}

<style>
    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
        padding-top: 0;
    }
</style>