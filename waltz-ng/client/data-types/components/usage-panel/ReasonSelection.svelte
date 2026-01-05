<script>
    import {
        deleteFlowReason,
        duplicateProposeFlowMessage,
        editDataTypeReason,
        existingProposeFlowId
    } from "../../../data-flow/components/svelte/propose-data-flow/propose-data-flow-store";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {createEventDispatcher, onMount} from "svelte";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import RatingPicker from "../../../common/svelte/RatingPicker.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {PROPOSAL_TYPES} from "../../../common/constants";


    export let ratingSchemeExtId;
    export let type;
    export let proposeDeleteFlow;


    const Modes = {
        SELECT: "SELECT",
        SELECTED: "SELECTED"
    }

    const dispatch = createEventDispatcher();

    let workingCopy = {rating: []};
    let activeMode = Modes.SELECT;

    let ratingScheme;

    onMount(async () => {
        if (type === PROPOSAL_TYPES.DELETE && $deleteFlowReason) {
            workingCopy = Object.assign({}, $deleteFlowReason);
        } else if (type === PROPOSAL_TYPES.EDIT && $editDataTypeReason) {
            workingCopy = Object.assign({}, $editDataTypeReason);
        }
    });

    function save() {
        if (type === PROPOSAL_TYPES.DELETE) {
            $deleteFlowReason = workingCopy;
            proposeDeleteFlow()
        } else if (type === PROPOSAL_TYPES.EDIT) {
            $editDataTypeReason = workingCopy;
        }
    }

    $: ratingSchemeCall = ratingSchemeStore.loadAll();
    $: ratingScheme = $ratingSchemeCall?.data.filter(t => t.externalId === ratingSchemeExtId)[0];

    function onRatingsSelect(evt) {
        workingCopy.rating = evt.detail;
        const emittedEvent = {ratingSchemeItems: workingCopy.rating};
        dispatch("select", emittedEvent);
        if (type === PROPOSAL_TYPES.EDIT) {
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
        Select a reason for proposing to {type.toLowerCase()} the data flow.
    </div>

    <form on:submit|preventDefault={save}>
        {#if ratingScheme}
            {#if ratingScheme.ratings?.length}
                <RatingPicker scheme={ratingScheme}
                              isMultiSelect={false}
                              selectedRatings={workingCopy.rating}
                              on:select={onRatingsSelect}/>
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
        {#if type === PROPOSAL_TYPES.EDIT}
            <div class="small">
                <NoData type="info">
                    This will affect all associated physical flows.
                </NoData>
            </div>
        {/if}

        {#if type === PROPOSAL_TYPES.DELETE}
            <div style="display: flex; justify-content: flex-end;margin: 0;">
                <button class="btn btn-sm btn-primary">
                    Submit
                </button>
            </div>
        {/if}

        {#if $duplicateProposeFlowMessage}
            <div style="margin:20px 0px">
                <NoData type="error">
                    {$duplicateProposeFlowMessage}
                    <br>
                    <a href={$existingProposeFlowId} target="_blank" rel="noreferrer">Go to Flow</a>
                </NoData>
            </div>
        {/if}

    </form>
{/if}