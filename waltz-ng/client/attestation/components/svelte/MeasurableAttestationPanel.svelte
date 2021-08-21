<script>
    import {attestationInstanceStore} from "../../../svelte-stores/attestation-instance-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import DateTime from "../../../common/svelte/DateTime.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {entity} from "../../../common/services/enums/entity";
    import _ from "lodash";

    export let primaryEntityRef;

    $: latestattestationsCall = attestationInstanceStore.findLatestMeasurableAttestations(primaryEntityRef);
    $: measurableAttestationInstances = $latestattestationsCall.data;

</script>


<div class="help-block">
    <Icon name="info-circle">
    </Icon>
    The below table shows the latest attestation completed for each viewpoint taxonomy.
    If a run has been assigned but never completed, the 'attested at' and 'attested by' appear blank.
    Some viewpoint categories may not appear as no run has ever been issued.
</div>

{#if _.isEmpty(measurableAttestationInstances)}
    <NoData>
        No viewpoint category attestation runs have been issued for this {_.get(entity, [primaryEntityRef.kind, 'name'], 'entity')}
    </NoData>
{:else}
<table class="table table-condensed">
    <thead>
        <tr>
            <th>Category</th>
            <th>Last Attested At</th>
            <th>Last Attested By</th>
            <th>Run Name</th>
            <th>Run Issued On</th>
            <th>Run Due Date</th>
        </tr>
    </thead>

    <tbody>
        {#each measurableAttestationInstances as attestation}
            <tr>
                <td><EntityLink showIcon={false} ref={attestation.categoryRef}/></td>
                <td>
                    {#if attestation.attestedAt}
                        <DateTime relative={true}
                                  formatStr="yyyy-MM-DD"
                                  dateTime={attestation.attestedAt}/>
                    {:else}
                        -
                    {/if}
                </td>
                <td>{attestation.attestedBy || '-'}</td>
                <td><EntityLink showIcon={false} ref={attestation.attestationRunRef}/></td>
                <td>{attestation.issuedOn}</td>
                <td>{attestation.dueDate}</td>
            </tr>
        {/each}
    </tbody>
</table>
{/if}