<script>
    import _ from "lodash";
    import StepHeader from "../../../../physical-flows/svelte/StepHeader.svelte";
    import {expandedSections, proposalReason} from "./propose-data-flow-store";
    import {determineExpandedSections, sections} from "./propose-data-flow-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {createEventDispatcher, onMount} from "svelte";
    import {ratingSchemeStore} from "../../../../svelte-stores/rating-schemes";
    import RatingPicker from "../../../../common/svelte/RatingPicker.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";

    export let ratingSchemeExtId;

    const Modes = {
        SELECT: "SELECT",
        SELECTED: "SELECTED"
    }

    const dispatch = createEventDispatcher();

    let workingCopy = {rating: []};
    let activeMode = Modes.SELECT;

    onMount(() => {
        if ($proposalReason) {
            workingCopy = Object.assign({}, $proposalReason);
        }
    });

    function save() {
        $proposalReason = workingCopy;
        openNextSection();
    }

    function openNextSection() {
        const flowSectionOpen = _.includes($expandedSections, sections.ROUTE);
        if (!flowSectionOpen) {
            $expandedSections = _.concat($expandedSections, sections.ROUTE)
        }
    }

    function toggleSection() {
        $expandedSections = determineExpandedSections($expandedSections, sections.REASON);
    }

    function editSpec() {
        if ($proposalReason.rating) {
            workingCopy = {rating: []};
            // toasts.info("Instead of altering the original specification, a new one will be created for this flow");
        }
        $proposalReason = null;
        activeMode = Modes.SELECT;
    }

    $: ratingSchemeCall = ratingSchemeStore.loadAll();
    $: done = workingCopy.rating[0] && true;

    $: expanded = _.includes($expandedSections, sections.REASON);
    $: ratingScheme = $ratingSchemeCall?.data.filter(t => t.externalId === ratingSchemeExtId)[0];

    function onRatingsSelect(evt) {
        workingCopy.rating = evt.detail;
        const emittedEvent = {ratingSchemeItems: workingCopy.rating};
        dispatch("select", emittedEvent);
    }

</script>

<StepHeader label="Reason"
            icon="lightbulb-o"
            checked={$proposalReason}
            {expanded}
            onToggleExpanded={toggleSection}/>

{#if expanded}
    <div class="step-body">

        {#if $proposalReason}

            <div>
                <div style="font-weight: lighter">Selected Reason: {$proposalReason.rating[0].name}</div>
            </div>

            <button class="btn btn-skinny"
                    style="padding-top: 1em"
                    on:click={() => editSpec()}>
                <Icon name="times"/>
                Pick a different reason
            </button>

        {:else}

            {#if activeMode === Modes.SELECTED}
                <div>
                    Rating already selected
                </div>
            {:else if activeMode === Modes.SELECT}

                <div class="help-block">
                    <Icon name="info-circle"/>
                    Select a reason for proposing the data flow.
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

                    <button class="btn btn-skinny"
                            disabled={!done}
                            on:click={() => save()}>
                        Done
                    </button>
                </form>
            {/if}
        {/if}
    </div>
{/if}

<style>
    .step-body {
        padding-left: 1em;
    }
</style>