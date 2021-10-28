<script>
    import {selectedDecorator} from "./flow-decorator-store";
    import {physicalFlowStore} from "../../../svelte-stores/physical-flow-store";
    import {truncate} from "../../../common/string-utils";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";

    $: physicalFlowCall = physicalFlowStore.findUnderlyingPhysicalFlows($selectedDecorator?.dataFlowId, $selectedDecorator?.dataTypeId);
    $: physicalFlows = $physicalFlowCall.data;

</script>

<div class="small help-block">
    Physical flows sharing data type:
    <EntityLink ref={$selectedDecorator.decoratorEntity}/>:
</div>
{#if !_.isEmpty(physicalFlows)}
    <div class:waltz-scroll-region-250={_.size(physicalFlows) > 6}>
        <table class="table table-condensed table-hover small">
            <thead>
                <th style="width: 20%">Source</th>
                <th style="width: 20%">Target</th>
                <th style="width: 20%">Spec</th>
                <th style="width: 20%">External Id</th>
                <th style="width: 20%">Description</th>
            </thead>
            <tbody>
            {#each physicalFlows as flow}
                <tr>
                    <td>{flow.source?.name}</td>
                    <td>{flow.target?.name}</td>
                    <td><EntityLink ref={flow.specification}/></td>
                    <td>{truncate(flow.physicalFlowExternalId) || '-'}</td>
                    <td>{truncate(flow.physicalFlowDescription) || '-'}</td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
{:else}
    <NoData>There are no physical flows sharing this data type</NoData>
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