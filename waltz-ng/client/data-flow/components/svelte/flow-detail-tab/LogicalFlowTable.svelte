<script>

    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../../common";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import _ from "lodash";
    import {selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import {truncate} from "../../../../common/string-utils";
    import Tooltip from "../../../../common/svelte/Tooltip.svelte";
    import DataTypeTooltipContent from "./DataTypeTooltipContent.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let logicalFlows = [];
    export let assessments;

    let qry;

    $: visibleFlows = _.filter(logicalFlows, d => d.visible);

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

    function mkDataTypeString(dataTypes) {
        return _
            .chain(dataTypes)
            .map(d => d.decoratorEntity.name)
            .orderBy(d => d)
            .join(", ")
            .value();
    }

    function selectLogicalFlow(flow) {
        $selectedLogicalFlow = flow;
        $selectedPhysicalFlow = null;
    }

    function mkDataTypeTooltipProps(row) {
        return {
            decorators: row.dataTypesForLogicalFlow
        }
    }

</script>

<h4>
    Logical Flows
    <span class="small">
        (
        {#if _.size(flowList) !== _.size(logicalFlows)}
            {_.size(flowList)} /
        {/if}
        {_.size(logicalFlows)}
        )
    </span>
</h4>

<div>
    <SearchInput bind:value={qry}/>
</div>
<div class="table-container"
     class:waltz-scroll-region-350={_.size(logicalFlows) > 10}>
    <table class="table table-condensed small"
           style="margin-top: 1em">
        <thead>
        <tr>
            <th nowrap="nowrap" style="width: 20em">Source</th>
            <th nowrap="nowrap" style="width: 20em">Source External ID</th>
            <th nowrap="nowrap" style="width: 20em">Target</th>
            <th nowrap="nowrap" style="width: 20em">Target External ID</th>
            <th nowrap="nowrap" style="width: 20em; max-width: 20em">Data Types</th>
            {#each assessments as defn}
                <th nowrap="nowrap" style="width: 20em">{defn.name}</th>
            {/each}
        </tr>
        </thead>
        <tbody>
        {#each flowList as flow}
            <tr class="clickable"
                on:click={() => selectLogicalFlow(flow)}>
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
                    <Tooltip content={DataTypeTooltipContent}
                             props={mkDataTypeTooltipProps(flow)}>
                        <svelte:fragment slot="target">
                            <span>{truncate(mkDataTypeString(flow.dataTypesForLogicalFlow), 30)}</span>
                        </svelte:fragment>
                    </Tooltip>
                </td>
                {#each assessments as defn}
                    {@const assessmentRatingsForFlow = _.get(flow.ratingsByDefId, defn.id, [])}
                    <td>
                        <div class="rating-col">
                            {#each assessmentRatingsForFlow as rating}
                                <RatingIndicatorCell {...rating}/>
                            {/each}
                        </div>
                    </td>
                {/each}
            </tr>
        {:else}
            <tr>
                <td colspan={5 + _.size(assessments)}>
                    <NoData type="info">There are no logical flows to show, these may have been filtered.</NoData>
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
        border-collapse: separate;
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

    .rating-col {
        display: flex;
        gap: 1em;
    }


</style>