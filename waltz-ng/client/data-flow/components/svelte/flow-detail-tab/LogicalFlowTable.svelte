<script>

    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../../common";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import _ from "lodash";
    import {selectedLogicalFlow} from "./flow-details-store";

    export let logicalFlows;
    export let assessments;

    let qry;
    let visibleFlows;

    $: visibleFlows = _.isEmpty(qry)
        ? logicalFlows
        : termSearch(
            logicalFlows,
            qry,
            [
                "logicalFlow.source.name",
                "logicalFlow.source.externalId",
                "logicalFlow.target.name",
                "logicalFlow.target.externalId"
            ]);

    $: console.log({logicalFlows, assessments});

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
    }

</script>


<div>
    <SearchInput bind:value={qry}/>
</div>
<div class="table-container"
     class:waltz-scroll-region-350={_.size(logicalFlows) > 10}>
    <table class="table table-condensed"
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
        {#each visibleFlows as flow}
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
                    {mkDataTypeString(flow.dataTypesForLogicalFlow)}
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
        {/each}
        </tbody>
    </table>
</div>


<style>

    table {
        display: table;
        white-space: nowrap;
    }

    .table-container {
        overflow-x: auto;
    }

    .rating-col {
        display: flex;
        gap: 1em;
    }

</style>