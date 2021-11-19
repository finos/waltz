<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import {clearSelections, contextPanelMode, Modes, selectedClient, selectedClientArcs} from "./flow-decorator-store";
    import {createEventDispatcher} from "svelte";
    import EntityInfoPanel from "../../../common/svelte/info-panels/EntityInfoPanel.svelte";
    import {entity} from "../../../common/services/enums/entity";
    import NoData from "../../../common/svelte/NoData.svelte";
    import _ from "lodash";

    let selectedTab = "context";
    let dispatch = createEventDispatcher();

    function focusOnEntity(selectedEntity) {
        dispatch('selectEntity', selectedEntity);
    }

    function cancel() {
        clearSelections();
    }

    $: actualDataTypeCount = _
        .chain($selectedClientArcs)
        .flatMap(f => f.actualDataTypeIds)
        .uniq()
        .size()
        .value();

    $: rollupDataTypeCount = _
        .chain($selectedClientArcs)
        .flatMap(f => f.rollupDataTypeIds)
        .uniq()
        .size()
        .value();

</script>

<EntityInfoPanel primaryEntityRef={$selectedClient}>

    <div slot="post-title">
        <NoData type="info"
                style="padding-top: 1em; padding-bottom: 1em">
            This {entity[$selectedClient.kind].name} has
            <span title={"These roll up to " + rollupDataTypeCount + " parent data types"}>
                {actualDataTypeCount} data types
            </span> and {_.size($selectedClient.physicalFlows)} physical flows.
            <button class="btn btn-skinny"
                    on:click={() => $contextPanelMode = Modes.FLOW_DETAIL}>
                View flow detail.
            </button>
        </NoData>
    </div>


</EntityInfoPanel>

<br>

<div style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
    <ul>
        <li>
            <button class="btn btn-skinny" on:click={() => focusOnEntity($selectedClient)}>
                <Icon name="dot-circle-o"/>Focus diagram on {$selectedClient?.name}
            </button>
        </li>
        <li>
            <button class="btn btn-skinny"
                     on:click={() => cancel()}>
                <Icon name="times"/>Cancel
            </button>
        </li>
    </ul>
</div>


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