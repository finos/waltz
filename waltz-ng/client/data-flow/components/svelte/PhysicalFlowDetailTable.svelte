<script>
    import {selectedClient, layoutDirection, layoutDirections} from "./scroll-store";
    import _ from "lodash";
    import {truncate} from "../../../common/string-utils";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";

    export let parentEntity;

    $: physicalFlows = $selectedClient.physicalFlows

    $: source = $layoutDirection === layoutDirections.clientToCategory ? $selectedClient.name : parentEntity.name
    $: target = $layoutDirection === layoutDirections.clientToCategory ? parentEntity.name : $selectedClient.name

</script>

{#if !_.isEmpty(physicalFlows)}
    <div>
        Physical Flows from {source} to {target}:
    </div>
    <div class:waltz-scroll-region-250={_.size(physicalFlows) > 6}>
        <table class="table table-condensed small">
            <colgroup>
                <col style="width: 25%;">
                <col style="width: 25%;">
                <col style="width: 25%;">
                <col style="width: 25%;">
            </colgroup>
            <thead>
            <tr>
                <th>External Id</th>
                <th>Transport Kind</th>
                <th>Frequency</th>
                <th>Criticality</th>
            </tr>
            </thead>
            <tbody>
            {#each physicalFlows as physFlow}
                <tr>
                    <td title={physFlow.externalId}>
                        <EntityLink ref={Object.assign({}, physFlow, { name: truncate(physFlow.externalId, 12) || "-" })}/>
                    </td>
                    <td title={physFlow.transport}>{truncate(physFlow.transport, 12)}</td>
                    <td title={physFlow.frequency}>{physFlow.frequency}</td>
                    <td title={physFlow.criticality}>{physFlow.criticality}</td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
{:else}
    <NoData>There are no physical flows associated to this logical flow</NoData>
{/if}