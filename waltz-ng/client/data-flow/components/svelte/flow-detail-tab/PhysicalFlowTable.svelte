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
    import NoData from "../../../../common/svelte/NoData.svelte";
    import DataTypeTooltipContent from "./DataTypeMiniTable.svelte";
    import {truncate} from "../../../../common/string-utils";
    import Tooltip from "../../../../common/svelte/Tooltip.svelte";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import EntityIcon from "../../../../common/svelte/EntityIcon.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";

    export let physicalFlows = [];
    export let flowClassifications = [];
    export let assessmentDefinitions = [];

    function selectPhysicalFlow(flow) {
        if ($selectedPhysicalFlow === flow) {
            $selectedPhysicalFlow = null;
        } else {
            $selectedPhysicalFlow = flow;
            $selectedLogicalFlow = flow;
        }
    }

    function mkDataTypeTooltipProps(row) {
        row.dataTypesForSpecification;

        const ratingByDataTypeId = _.reduce(
            row.dataTypesForLogicalFlow,
            (acc, d) => {
                acc[d.decoratorEntity.id] = d.rating;
                return acc;
            },
            {});

        const decorators = _.map(
            row.dataTypesForSpecification,
            d => Object.assign(
                {},
                d,
                { rating: ratingByDataTypeId[d.decoratorEntity?.id] }));

        return {
            decorators,
            flowClassifications
        };
    }

    function mkDataTypeString(dataTypes) {
        return _
            .chain(dataTypes)
            .map(d => d.decoratorEntity.name)
            .orderBy(d => d)
            .join(", ")
            .value();
    }

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
                (f) => _.get(f.logicalFlow.source, ["name"], ""),
                (f) => _.get(f.logicalFlow.source, ["externalId"], ""),
                (f) => _.get(f.logicalFlow.target, ["name"], ""),
                (f) => _.get(f.logicalFlow.target, ["externalId"], ""),
                (f) => _.get(f.physicalFlow, ["name"], ""),
                (f) => _.get(f.physicalFlow, ["externalId"], ""),
                (f) => _.get(f.physicalFlow, ["frequency"], ""),
                (f) => _.get(f.physicalFlow, ["transport"], ""),
                (f) => _.get(f.physicalFlow, ["criticality"], ""),
                (f) => _.get(f.specification, ["name"], ""),
                (f) => _.chain(f.physicalFlowRatingsByDefId).values().flatten().map(d => d.name).join(" ").value(),
                (f) => _.chain(f.physicalSpecRatingsByDefId).values().flatten().map(d => d.name).join(" ").value(),
                (f) => _.chain(f.dataTypesForSpecification).map(d => _.get(d, ["decoratorEntity", "name"])).join(" ").value()
            ]);

    $: defs = _.filter(
        assessmentDefinitions,
        d => d.entityKind === 'PHYSICAL_FLOW' || d.entityKind === 'PHYSICAL_SPECIFICATION');

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
    <table class="table table-condensed small table-hover"
           style="margin-top: 1em">
        <thead>
        <tr>
            <th nowrap="nowrap" style="width: 1em"></th>
            <th nowrap="nowrap" style="width: 20em">Source</th>
            <th nowrap="nowrap" style="width: 20em">Src Ext ID</th>
            <th nowrap="nowrap" style="width: 20em">Target</th>
            <th nowrap="nowrap" style="width: 20em">Target Ext ID</th>
            <th nowrap="nowrap" style="width: 20em; max-width: 20em">Name</th>
            <th nowrap="nowrap" style="width: 20em">External ID</th>
            <th nowrap="nowrap" style="width: 20em">Data Types</th>
            <th nowrap="nowrap" style="width: 20em">Criticality</th>
            <th nowrap="nowrap" style="width: 20em">Frequency</th>
            <th nowrap="nowrap" style="width: 20em">Transport Kind</th>
            {#each defs as defn}
                <th nowrap="nowrap" style="width: 20em">
                    <EntityIcon kind={defn.entityKind}/>
                    {defn.name}
                </th>
            {/each}
        </tr>
        </thead>
        <tbody>
        {#each flowList as flow}
            {@const ratingsForFlowByDefId = _.merge(flow.physicalSpecRatingsByDefId, flow.physicalFlowRatingsByDefId)}

            <tr class="clickable"
                class:selected={$selectedPhysicalFlow === flow}
                on:click={() => selectPhysicalFlow(flow)}>
                <td>
                    {#if flow.physicalFlow.isReadOnly}
                        <span style="color: grey"
                              title={`Flow has been marked as readonly (provenance: ${flow.physicalFlow.provenance}, last updated by: ${flow.physicalFlow.lastUpdatedBy})`}>
                            <Icon name="lock"
                                  fixed-width="true"/>
                        </span>
                    {/if}
                </td>
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
                    {flow.physicalFlow.name || flow.specification?.name || ""}
                </td>
                <td>
                    {flow.physicalFlow.externalId || ""}
                </td>
                <td>
                    <Tooltip content={DataTypeTooltipContent}
                             trigger={"mouseenter"}
                             props={mkDataTypeTooltipProps(flow)}>
                        <svelte:fragment slot="target">
                            <span>{truncate(mkDataTypeString(flow.dataTypesForSpecification), 30)}</span>
                        </svelte:fragment>
                    </Tooltip>
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
                {#each defs as defn}
                    {@const assessmentRatingsForFlow = _.get(ratingsForFlowByDefId, defn.id, [])}
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
                <td colspan={9 + _.size(defs)}>
                    <NoData type="info">There are no physical flows to show, these may have been filtered.</NoData>
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