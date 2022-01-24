<script>
    import {selectedCategory} from "./flow-decorator-store";
    import _ from "lodash";
    import {truncate} from "../../../common/string-utils";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {physicalFlowStore} from "../../../svelte-stores/physical-flow-store";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let logicalFlowId;

    $: physicalFlowCall = physicalFlowStore.findUnderlyingPhysicalFlows(logicalFlowId);
    $: physicalFlows = $physicalFlowCall.data;

    $: filteredFlows = $selectedCategory
        ? _.filter(physicalFlows, f => _.some(f.dataTypes, dt => dt.id === $selectedCategory.id))
        : physicalFlows

</script>


{#if _.isEmpty(physicalFlows)}
    <NoData>There are no physical flows associated to this logical flow</NoData>
{:else}
    {#if _.size(filteredFlows) !== _.size(physicalFlows)}
        <NoData type="warning">
            <Icon name="exclamation-triangle"/>
            These flows have been filtered by data type: ({$selectedCategory.name}).
            <button class="btn btn-skinny"
                    on:click={() => $selectedCategory = null}>
                Clear all filters.
            </button>
        </NoData>
    {/if}
    <div class:waltz-scroll-region-250={_.size(filteredFlows) > 6}>
        <table class="table table-condensed small">
            <colgroup>
                <col style="width: 40%;">
                <col style="width: 30%;">
                <col style="width: 30%;">
            </colgroup>
            <thead>
            <tr>
                <th>Physical Flow</th>
                <th>External Id</th>
                <th>Criticality</th>
            </tr>
            </thead>
            <tbody>
            {#each filteredFlows as physFlow}
                <tr>
                    <td title={physFlow.specification}>
                        <EntityLink
                            ref={Object.assign({}, physFlow.specification, { name: truncate(physFlow.specification.name, 12) || "-" })}/>
                    </td>
                    <td title={physFlow.physicalFlowExternalId}>{truncate(physFlow.physicalFlowExternalId, 12)}</td>
                    <td title={physFlow.criticality}>{physFlow.criticality}</td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
{/if}