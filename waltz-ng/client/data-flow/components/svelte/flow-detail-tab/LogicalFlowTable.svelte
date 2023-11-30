<script>
    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../../common";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import _ from "lodash";
    import {filters, selectedLogicalFlow, selectedPhysicalFlow, updateFilters} from "./flow-details-store";
    import {truncate} from "../../../../common/string-utils";
    import Tooltip from "../../../../common/svelte/Tooltip.svelte";
    import DataTypeTooltipContent from "./DataTypeMiniTable.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let logicalFlows = [];
    export let flowClassifications = [];
    export let assessmentDefinitions = [];

    let qry;
    let selectionLatchOpen = true;
    let defs = [];

    function isSameFlow(a, b) {
        const aId = _.get(a, ["logicalFlow", "id"]);
        const bId = _.get(b, ["logicalFlow", "id"]);
        return aId === bId;
    }

    function mkDataTypeString(dataTypes) {
        return _
            .chain(dataTypes)
            .map(d => d.decoratorEntity.name)
            .orderBy(d => d)
            .join(", ")
            .value();
    }

    function selectLogicalFlow(flow) {

        const addFilter = () => {
            const lfId = flow.logicalFlow.id;
            updateFilters(
                "SELECTED_LOGICAL",
                {
                    id: "SELECTED_LOGICAL",
                    test: (r) => r.logicalFlow.id === lfId
                });
        };

        const removeFilter = () => {
            $filters = _.reject($filters, d => d.id === "SELECTED_LOGICAL");
        };

        if (isSameFlow($selectedLogicalFlow, flow)) {
            if ($selectedPhysicalFlow && selectionLatchOpen) {
                selectionLatchOpen = false;
                addFilter();
            } else {
                $selectedLogicalFlow = null;
                $selectedPhysicalFlow = null;
                selectionLatchOpen = true;
                removeFilter();
            }
        } else {
            selectionLatchOpen = true;
            $selectedLogicalFlow = flow;
            $selectedPhysicalFlow = null;
            addFilter()
        }
    }

    function mkDataTypeTooltipProps(row) {
        return {
            decorators: row.dataTypesForLogicalFlow,
            flowClassifications
        };
    }

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

    $: defs = _.filter(
        assessmentDefinitions,
        d => d.entityKind === 'LOGICAL_DATA_FLOW');
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
    <table class="table table-condensed small table-hover"
           style="margin-top: 1em">
        <thead>
        <tr>
            <th nowrap="nowrap" style="width: 20em">Source</th>
            <th nowrap="nowrap" style="width: 20em">Src Ext ID</th>
            <th nowrap="nowrap" style="width: 20em">Target</th>
            <th nowrap="nowrap" style="width: 20em">Target Ext ID</th>
            <th nowrap="nowrap" style="width: 20em; max-width: 20em">Data Types</th>
            {#each defs as defn}
                <th nowrap="nowrap" style="width: 20em">{defn.name}</th>
            {/each}
        </tr>
        </thead>
        <tbody>
        {#each flowList as flow}
            <tr class="clickable"
                class:selected={isSameFlow($selectedLogicalFlow, flow)}
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
                             trigger={"mouseenter"}
                             props={mkDataTypeTooltipProps(flow)}>
                        <svelte:fragment slot="target">
                            <span>{truncate(mkDataTypeString(flow.dataTypesForLogicalFlow), 30)}</span>
                        </svelte:fragment>
                    </Tooltip>
                </td>
                {#each defs as defn}
                    {@const assessmentRatingsForFlow = _.get(flow.logicalFlowRatingsByDefId, defn.id, [])}
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
                <td colspan={5 + _.size(defs)}>
                    <NoData type="info">There are no logical flows to show, these may have been filtered.</NoData>
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
</div>


<style>
    .selected {
        background: #eefaee !important;
    }

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
        z-index: 1;
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