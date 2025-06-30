<script>
    import _ from "lodash";
    import {attestationInstanceStore} from "../../../svelte-stores/attestation-instance-store";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {permissionGroupStore} from "../../../svelte-stores/permission-group-store";
    import {mkChunks} from "../../../common/list-utils";
    import MeasurableAttestationSubPanel from "./MeasurableAttestationSubPanel.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import {mergeCategoriesWithLatestAttestations} from "./measurable-attestation-utils";

    export let primaryEntityRef;
    export let onAttestationInitiated = (category) => console.log("Default Handler: onAttestationInitiated", {category});
    export let unAttestedChanges;

    function attestationInitiated(evt) {
        onAttestationInitiated(evt.detail);
    }

    function getUnAttestedChanges() {
        return _.filter(unAttestedChanges, change => change.childKind === 'MEASURABLE');
    }

    let chunkedCategories = [];

    $: latestAttestationsCall = attestationInstanceStore.findLatestMeasurableAttestations(primaryEntityRef, true);
    $: supportedCategoriesCall = permissionGroupStore.findSupportedMeasurableCategoryAttestations(primaryEntityRef);
    $: chunkedCategories = mkChunks(
        mergeCategoriesWithLatestAttestations(
            $supportedCategoriesCall.data,
            $latestAttestationsCall.data),
        2);
    $: changelog = getUnAttestedChanges();
</script>


{#if $supportedCategoriesCall?.status === 'loading' || $latestAttestationsCall?.status === 'loading'}
    <LoadingPlaceholder/>
{:else}
    {#each chunkedCategories as chunks}
        <div class="row">
            {#each chunks as chunk}
                <div class="col-sm-6">
                    <MeasurableAttestationSubPanel on:attestationInitiated={attestationInitiated}
                                                   measurableCategory={chunk.qualifierReference}
                                                   latestAttestation={chunk.latestAttestation}
                                                   isAttestable={chunk.hasPermission}
                                                   unAttestedChanges={changelog}/>
                </div>
            {/each}
        </div>
    {:else}
        <NoData>
            There are no attestable viewpoints
        </NoData>
    {/each}
{/if}