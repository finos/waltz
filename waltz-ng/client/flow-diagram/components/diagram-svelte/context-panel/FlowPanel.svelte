<script>

    import {toGraphId} from "../../../flow-diagram-utils";
    import {createEventDispatcher} from "svelte";
    import AddAnnotationSubPanel from "./AddAnnotationSubPanel.svelte";
    import AddPhysicalFlowSubPanel from "./AddPhysicalFlowSubPanel.svelte";
    import model from "../store/model";
    import {physicalFlowStore} from "../../../../svelte-stores/physical-flow-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {mkRef} from "../../../../common/entity-utils";
    import {physicalSpecStore} from "../../../../svelte-stores/physical-spec-store";
    import _ from "lodash";
    import {truncateMiddle} from "../../../../common/string-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";

    export let selected;
    export let canEdit;

    const dispatch = createEventDispatcher();

    $: selector = mkSelectionOptions(mkRef('LOGICAL_DATA_FLOW', selected.id), 'EXACT');
    $: physicalFlowCall = physicalFlowStore.findBySelector(selector);
    $: physicalSpecCall = physicalSpecStore.findBySelector(selector);

    $: physicalFlows = $physicalFlowCall.data
    $: physicalSpecs = $physicalSpecCall.data
    $: physicalSpecsById = _.keyBy(physicalSpecs, d => d.id);

    $: decorations = _.get($model.decorations, toGraphId(selected), []);

    $: flowIds = _.map(decorations, d => d.data.id);

    $: flows = _
        .chain(physicalFlows)
        .filter(d => _.includes(flowIds, d.id))
        .map(d => Object.assign({}, {flow: d, spec: _.get(physicalSpecsById, [d.specificationId], "unknown")}))
        .value();

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
        ADD_PHYSICAL_FLOW: "ADD_PHYSICAL_FLOW",
        ADD_ANNOTATION: "ADD_ANNOTATION",
        REMOVAL: "REMOVAL"
    };

    let activeMode = Modes.VIEW;


    function removeFlow() {
        model.removeFlow({id: toGraphId(selected), data: selected});
        cancel()
    }

    function cancel() {
        dispatch("cancel");
    }

</script>



<div style="margin-bottom: 1em">
    <strong>{selected.source.name} &rarr {selected.target.name}</strong>
    <div>
        {#if decorations.length > 0}
            This flow has {decorations.length} physical flow decorators associated.
        {:else}
            No physical flow decorators have been associated to this flow.
        {/if}
    </div>
</div>

{#if activeMode === Modes.VIEW}
<div>
    {#if decorations.length > 0}
        <table class="table table-condensed">
            <thead>
                <th width="50%">Name</th>
                <th width="50%">Ext Id</th>
            </thead>
            <tbody>
            {#each flows as flowInfo}
                <tr>
                    <td>{truncateMiddle(flowInfo.spec?.name, 30)}</td>
                    <td>{truncateMiddle(_.get(flowInfo, d => d.flow.externalId, "-"), 30)}</td>
                </tr>
            {/each}
            </tbody>
        </table>
    {/if}
</div>
<span>
    {#if canEdit}
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.EDIT}>
        <Icon name="pencil"/>Edit
    </button>
    {/if}
    |
    <button class="btn btn-skinny"
            on:click={() => cancel()}>
        Cancel
    </button>
</span>
{:else if activeMode === Modes.EDIT}
<ul>
    <li>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD_ANNOTATION}>
            <Icon name="plus"/>
            Add annotation
        </button>
    </li>
    <li>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD_PHYSICAL_FLOW}>
            <Icon name="pencil"/>
            Edit physical flows
        </button>
    </li>
    <li>
        <button class="btn btn-skinny"
                on:click={() => removeFlow()}>
            <Icon name="trash"/>
            Remove
        </button>
    </li>
    <li>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.VIEW}>
            Cancel
        </button>
    </li>
</ul>
{:else if activeMode === Modes.ADD_ANNOTATION}
    <AddAnnotationSubPanel {selected}
                           on:cancel={() => activeMode = Modes.VIEW}/>
{:else if activeMode === Modes.ADD_PHYSICAL_FLOW}
    <AddPhysicalFlowSubPanel {selected}
                             on:cancel={() => activeMode = Modes.VIEW}/>
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