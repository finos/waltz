<script>

    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../../common";
    import _ from "lodash";
    import {enumValueStore} from "../../../../svelte-stores/enum-value-store";
    import {nestEnums} from "../../../../common/svelte/enum-utils";
    import {
        toCriticalityName,
        toFrequencyKindName,
        toTransportKindName
    } from "../../../../physical-flows/svelte/physical-flow-registration-utils";
    import {selectedPhysicalFlow, selectedLogicalFlow} from "./flow-details-store";
    import {mkLogicalFromFlowDetails} from "./flow-detail-utils";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let physicalFlows;

    let qry;
    let visibleFlows;
    let enumsCall = enumValueStore.load();
    let nestedEnums;

    $: nestedEnums = nestEnums($enumsCall.data);

    $: visibleFlows = _.filter(physicalFlows, d => d.visible);

    $: flowList = _.isEmpty(qry)
        ? visibleFlows
        : termSearch(
            visibleFlows,
            qry,
            [
                "logicalFlow.source.name",
                "logicalFlow.source.externalId",
                "logicalFlow.target.name",
                "logicalFlow.target.externalId"
            ]);


    function selectPhysicalFlow(flow) {
        $selectedPhysicalFlow = flow;
        $selectedLogicalFlow = mkLogicalFromFlowDetails(flow);
    }

</script>


<h4>
    Physical Flows
    <span class="small">
        (
        {#if _.size(flowList) !== _.size(physicalFlows)}
            {_.size(flowList)} /
        {/if}
        {_.size(physicalFlows)}
        )
    </span>
</h4>

<div>
    <SearchInput bind:value={qry}/>
</div>
<div class="table-container"
     class:waltz-scroll-region-350={_.size(physicalFlows) > 10}>
    <table class="table table-condensed small"
           style="margin-top: 1em">
        <thead>
        <tr>
            <th nowrap="nowrap" style="width: 20em">Source</th>
            <th nowrap="nowrap" style="width: 20em">Source External ID</th>
            <th nowrap="nowrap" style="width: 20em">Target</th>
            <th nowrap="nowrap" style="width: 20em">Target External ID</th>
            <th nowrap="nowrap" style="width: 20em; max-width: 20em">Name</th>
            <th nowrap="nowrap" style="width: 20em">External ID</th>
            <th nowrap="nowrap" style="width: 20em">Criticality</th>
            <th nowrap="nowrap" style="width: 20em">Frequency</th>
            <th nowrap="nowrap" style="width: 20em">Transport Kind</th>
        </tr>
        </thead>
        <tbody>
        {#each flowList as flow}
            <tr class="clickable"
                on:click={() => selectPhysicalFlow(flow)}>
                <td>
                    {flow.logicalFlow.source.name}
                </td>
                <td>
                    {flow.logicalFlow.source.externalId}
                </td>
                <td>
                    {flow.logicalFlow.target.name}
                </td>
                <td>
                    {flow.logicalFlow.target.externalId}
                </td>
                <td>
                    {flow.physicalFlow.name || flow.specification.name}
                </td>
                <td>
                    {flow.physicalFlow.externalId || ""}
                </td>
                <td>
                    {toCriticalityName(nestedEnums, flow.physicalFlow.criticality)}
                </td>
                <td>
                    {toFrequencyKindName(nestedEnums, flow.physicalFlow.frequency)}
                </td>
                <td>
                    {toTransportKindName(nestedEnums, flow.physicalFlow.transport)}
                </td>
            </tr>
        {:else}
            <tr>
                <td colspan="9">
                    <NoData type="info">There are no physical flows to show, these may have been filtered.</NoData>
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
</div>


<style>

    table {
        display: table;
        white-space: nowrap;
        position: relative;
    }

    th {
        position: sticky;
        top: 0;
        background: white;
    }

    .table-container {
        overflow-x: auto;
        padding-top: 0;
    }


</style>