<script>
    import AddLogicalFlowSubPanel from "./AddLogicalFlowSubPanel.svelte";
    import {Directions} from "./panel-utils";
    import {createEventDispatcher} from "svelte";
    import {toGraphId} from "../../../flow-diagram-utils";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import AddAnnotationSubPanel from "./AddAnnotationSubPanel.svelte";
    import model from "../store/model";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {loadSvelteEntity} from "../../../../common/entity-utils";

    export let selected;
    export let canEdit;
    const dispatch = createEventDispatcher();

    $: nodeCall = selected && loadSvelteEntity(selected);

    $: node = $nodeCall.data;

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
        ADD_ANNOTATION: "ADD_ANNOTATION",
        ADD_UPSTREAM_FLOW: "ADD_UPSTREAM_FLOW",
        ADD_DOWNSTREAM_FLOW: "ADD_DOWNSTREAM_FLOW",
        REMOVAL: "REMOVAL"
    };

    let activeMode = Modes.VIEW;

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
<br>
{#if activeMode === Modes.VIEW && node}
    <div class="help-block">
        <p>{node.assetCode || node.externalId || "No external identifier"}</p>
        {node.description}
    </div>
    <div style="border-top: 1px solid #eee; margin-top:0.5em; padding-top:0.5em">
        {#if canEdit}
        <span>
            <button class="btn btn-skinny"
                    on:click={() => activeMode = Modes.EDIT}>
                Edit
            </button>
            |
        </span>
        {/if}
        <button class="btn btn-skinny"
                on:click={cancel}>
            Cancel
        </button>
    </div>
{:else if activeMode === Modes.EDIT}
    <ul>
        {#if canEdit}
            <li>
                <button class="btn btn-skinny"
                        on:click={() => activeMode = Modes.ADD_UPSTREAM_FLOW}>
                    <Icon name="sign-in"/>
                    Add upstream source
                </button>
            </li>
            <li>
                <button class="btn btn-skinny"
                        on:click={() => activeMode = Modes.ADD_DOWNSTREAM_FLOW}>
                    <Icon name="sign-out"/>
                    Add downstream target
                </button>
            </li>
            <li>
                <button class="btn btn-skinny"
                        on:click={() => activeMode = Modes.ADD_ANNOTATION}>
                    <Icon name="comment-o"/>
                    Add annotation
                </button>
            </li>
            <li>
                <button class="btn btn-skinny"
                        on:click={() => removeNode()}>
                    <Icon name="trash"/>
                    Remove
                </button>
            </li>
        {/if}
    </ul>
    <div class="context-panel-footer" >
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.VIEW}>
            <Icon name="fw"/>
            Cancel
        </button>
    </div>
{:else if activeMode === Modes.ADD_UPSTREAM_FLOW}
    <AddLogicalFlowSubPanel selected={selected}
                            direction={Directions.UPSTREAM}
                            on:cancel={() => activeMode = Modes.EDIT}/>
{:else if activeMode === Modes.ADD_DOWNSTREAM_FLOW}
    <AddLogicalFlowSubPanel selected={selected}
                            direction={Directions.DOWNSTREAM}
                            on:cancel={() => activeMode = Modes.EDIT}/>
{:else if activeMode === Modes.ADD_ANNOTATION}
    <AddAnnotationSubPanel selected={selected}
                           on:cancel={() => activeMode = Modes.EDIT}/>
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

    .context-panel-footer {
        border-top: 1px solid #eee;
        margin-top:0.5em;
        padding-top:0.5em;
    }
</style>