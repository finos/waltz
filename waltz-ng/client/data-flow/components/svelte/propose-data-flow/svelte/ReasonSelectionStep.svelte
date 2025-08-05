<script>
    import _ from "lodash";
    import StepHeader from "./StepHeader.svelte";
    import {enumValueStore} from "../../../../../svelte-stores/enum-value-store";
    import EnumSelect from "./EnumSelect.svelte";
    import PhysicalSpecificationSelector from "./PhysicalSpecificationSelector.svelte";
    import {physicalSpecStore} from "../../../../../svelte-stores/physical-spec-store";
    import {mkSelectionOptions} from "../../../../../common/selector-utils";
    import {expandedSections, proposalReason, nestedEnums} from "./propose-data-flow-store";
    import {
        determineExpandedSections,
        sections,
        toDataFormatKindName,
        toOptions
    } from "./propose-data-flow-utils";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import {createEventDispatcher, onMount} from "svelte";
    import toasts from "../../../../../svelte-stores/toast-store";
    import {ratingSchemeStore} from "../../../../../svelte-stores/rating-schemes";
    import RatingPicker from "../../../../../common/svelte/RatingPicker.svelte";

    const Modes = {
        SELECT: "SELECT",
        SELECTED: "SELECTED"
    }

    const dispatch = createEventDispatcher();

    export let primaryEntityRef;

    let workingCopy = {rating: []};
    let specificationCall;
    let activeMode = Modes.SELECT;
    let enumsCall = enumValueStore.load();

    onMount(() => {
        if ($proposalReason) {
            workingCopy = Object.assign({}, $proposalReason);
        }
    });

    function save() {
        $proposalReason = workingCopy;
        openNextSection();
    }

    function selectSpecification(evt) {
        $proposalReason = evt.detail;
        workingCopy = evt.detail;
        openNextSection();
    }

    function openNextSection() {
        const flowSectionOpen = _.includes($expandedSections, sections.FLOW);
        if (!flowSectionOpen) {
            $expandedSections = _.concat($expandedSections, sections.FLOW)
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

    $: {
        if (primaryEntityRef) {
            const selector = mkSelectionOptions(primaryEntityRef);
            specificationCall = physicalSpecStore.findBySelector(selector)
        }
    }

    $: specifications = $specificationCall?.data;
    $: enumsByType = _.groupBy($enumsCall.data, d => d.type);
    $: dataFormatKinds = toOptions(enumsByType, "DataFormatKind");
    $: ratingSchemeCall = ratingSchemeStore.getById(23924);

    $: done = workingCopy.rating[0] && true;

    $: expanded = _.includes($expandedSections, sections.REASON);
    $: ratingScheme = $ratingSchemeCall.data;
    $: console.log(workingCopy.rating);

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

                    <RatingPicker scheme={ratingScheme}
                                  isMultiSelect={false}
                                  selectedRatings={workingCopy.rating}
                                  on:select={onRatingsSelect}/>

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