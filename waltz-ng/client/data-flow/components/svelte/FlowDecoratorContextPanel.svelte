<script>
    import Icon from "../../../common/svelte/Icon.svelte";
    import {contextPanelMode, Modes, selectedDecorator} from "./flow-decorator-store";
    import EntityInfoPanel from "../../../common/svelte/info-panels/EntityInfoPanel.svelte";
    import {physicalFlowStore} from "../../../svelte-stores/physical-flow-store";
    import FlowDatatypeDetailTable from "./FlowDatatypeDetailTable.svelte";

    export let parentEntity;
    export let flowInfo;

    function cancel() {
        $contextPanelMode = Modes.DEFAULT
        $selectedDecorator = null;
    }

    $: physicalFlowCall = physicalFlowStore.findUnderlyingPhysicalFlows($selectedDecorator?.dataFlowId, $selectedDecorator?.dataTypeId);
    $: physicalFlows = $physicalFlowCall.data;

</script>

<EntityInfoPanel primaryEntityRef={$selectedDecorator.entityReference}>

    <div slot="post-header">
        <div style="margin-bottom: 1em;">

            <FlowDatatypeDetailTable />

        </div>
    </div>

</EntityInfoPanel>

<br>

<div style="border-top: 1px dotted #eee; padding-top: 0.2em; margin-top: 0.2em">
    <ul>
        <li>
            <button class="btn btn-skinny"
                     on:click={() => cancel()}>
                <Icon name="times"/>Cancel
            </button>
        </li>
    </ul>
</div>
