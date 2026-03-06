<script>
    import {
        deleteFlowReason,
        duplicateProposeFlowMessage,
        editDataTypeReason,
        editValidationMessage,
        existingProposeFlowId
    } from "../../../data-flow/components/svelte/propose-data-flow/propose-data-flow-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {createEventDispatcher, onMount} from "svelte";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import RatingPicker from "../../../common/svelte/RatingPicker.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {PROPOSAL_TYPES} from "../../../common/constants";
    import {proposeDataFlowRemoteStore} from "../../../svelte-stores/propose-data-flow-remote-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import DropdownPicker from "../../../common/svelte/DropdownPicker.svelte";


    export let ratingSchemeExtId;
    export let proposalType;
    export let proposeDeleteFlow;
    export let physicalFlowId;
    export let logicalFlowId;
    export let cancelProposeDeleteFlow;

    const Modes = {
        SELECT: "SELECT",
        SELECTED: "SELECTED"
    }

    const dispatch = createEventDispatcher();

    let workingCopy = {rating: []};
    let activeMode = Modes.SELECT;

    let ratingScheme;

    onMount(async () => {
        if (proposalType === PROPOSAL_TYPES.DELETE && $deleteFlowReason) {
            workingCopy = Object.assign({}, $deleteFlowReason);
        } else if (proposalType === PROPOSAL_TYPES.EDIT && $editDataTypeReason) {
            workingCopy = Object.assign({}, $editDataTypeReason);
        }
    });

    function save(event) {
        if (proposalType === PROPOSAL_TYPES.DELETE) {
            if (event?.submitter?.dataset?.action !== "submit") {
                return;
            }
            $deleteFlowReason = workingCopy;
            proposeDeleteFlow()
        } else if (proposalType === PROPOSAL_TYPES.EDIT) {
            $editDataTypeReason = workingCopy;
        }
    }


    function cancel() {
        cancelProposeDeleteFlow(false);
        $deleteFlowReason = null;
        $duplicateProposeFlowMessage = null;
        $existingProposeFlowId = null;
    }

    $: ratingSchemeCall = ratingSchemeStore.loadAll();
    $: ratingScheme = $ratingSchemeCall?.data.filter(t => t.externalId === ratingSchemeExtId)[0];

    $: physcialFlowCountStore = physicalFlowId
        ? proposeDataFlowRemoteStore.getPhysicalFlowsCountForAssociatedLogicalFlow(physicalFlowId, false)
        : null;

    $: physicalFlowCount = $physcialFlowCountStore?.data
    $: ratingItems = (ratingScheme?.ratings ?? []).map(r => ({
        ...r,
        displayText: `${r?.name ?? ""}-${r?.description ?? ""}`
    }));

    $: selectedReason = workingCopy.rating?.[0] ?? null;

    function onReasonSelect(item) {
        workingCopy.rating = item ? [item] : [];
        dispatch("select", { ratingSchemeItems: workingCopy.rating });
        if (proposalType === PROPOSAL_TYPES.EDIT) {
            save();
        }
    }

</script>

{#if activeMode === Modes.SELECTED}
    <div>
        Rating already selected
    </div>
{:else if activeMode === Modes.SELECT}
    <div class="help-block">
        <Icon name="info-circle"/>
        Select a reason for proposing to {proposalType.toLowerCase()} the data flow.
    </div>

    <form on:submit|preventDefault={save}>
        {#if ratingScheme}
            {#if ratingScheme.ratings?.length}
                <div class="reason-dropdown">
                    <DropdownPicker items={ratingItems}
                                    selectedItem={selectedReason}
                                    onSelect={onReasonSelect}
                                    defaultMessage={"Select a reason"}/>
                </div>
            {:else}
                <NoData>Reasons have not been defined.</NoData>
            {/if}
        {:else}
            {#if ratingSchemeExtId}
                <div>Loading reasons...</div>
            {:else}
                <NoData>Reasons have not been defined.</NoData>
            {/if}
        {/if}
        {#if proposalType === PROPOSAL_TYPES.EDIT}
            <div class="small">
                <NoData type="info">
                    This will affect all associated physical flows.
                </NoData>
            </div>
            {#if $editValidationMessage}
                <div class="small">
                    <NoData type="error">
                        {$editValidationMessage}
                    </NoData>
                </div>
            {/if}
        {/if}

        {#if proposalType === PROPOSAL_TYPES.DELETE}
            {#if physicalFlowCount===1}
                <NoData type="error">
                    <Icon name="warning" style="padding: 1em"/>
                    This is the last physical flow associated with the logical flow.
                    <br>This will lead to the deletion of the physical as well as the
                    <EntityLink ref={{ kind: "LOGICAL_DATA_FLOW", id: logicalFlowId, name: "logical flow" }}
                                showIcon={false}/>.
                </NoData>
                <br>
            {/if}
            <div style="display: flex; justify-content: flex-end;margin: 0;gap: 0.5rem">
                <button class="btn btn-sm btn-primary"
                        data-action="submit"
                        disabled={!workingCopy?.rating?.length || $existingProposeFlowId}>
                    Submit
                </button>
                <button type="button" class="btn btn-sm" on:click={cancel}>
                    Cancel
                </button>
            </div>
        {/if}

        {#if $duplicateProposeFlowMessage}
            <div style="margin:20px 0px">
                <NoData type="error">
                    {$duplicateProposeFlowMessage}
                    <br>
                    <a href={$existingProposeFlowId} rel="noreferrer">Go to Flow</a>
                </NoData>
            </div>
        {/if}

    </form>
{/if}

<style>
    .reason-dropdown {
        margin-bottom: 1rem;
    }
</style>