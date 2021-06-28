<script>
    import _ from "lodash";
    import {toGraphNode} from "../../../flow-diagram-utils";
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import model from "../store/model";
    import {positions} from "../store/layout";
    import {createEventDispatcher} from "svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";

    const dispatch = createEventDispatcher();

    const Modes = {
        SELECTING: "SELECTING",
        ADDED: "ADDED"
    };

    function cancel() {
        dispatch("cancel");
    }

    function saveNode(e) {
        const node = toGraphNode(e.detail);

        model.addNode(node);
        positions.move({
            id: node.id,
            dx: _.random(-80, 80),
            dy: _.random(50, 80)
        });

        mode = Modes.ADDED;
    }

    let mode = Modes.SELECTING;

</script>

<div>
    {#if mode === Modes.SELECTING}
        <div>
            <strong>Add a new node:</strong>
        </div>
        <EntitySearchSelector on:select={saveNode}
                              placeholder="Search for source"
                              entityKinds={['APPLICATION', 'ACTOR']}>
        </EntitySearchSelector>
    {:else if mode === Modes.ADDED}
        <strong>Added new node</strong>
        <br>
        <button class="btn-skinny"
                on:click={() => mode = Modes.SELECTING}>
            <Icon name="plus"/>
            Add another
        </button>
    {/if}
    <div class="context-panel-footer">
        <button class="btn btn-skinny"
                on:click={cancel}>
            Cancel
        </button>
    </div>
</div>

<style>

    .context-panel-footer {
        border-top: 1px solid #eee;
        margin-top:0.5em;
        padding-top:0.5em;
    }
</style>