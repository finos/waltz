<script>
    import AssessmentDefinitionPicker from "../../../../../report-grid/components/svelte/pickers/AssessmentDefinitionPicker.svelte";
    import Markdown from "../../../../../common/svelte/Markdown.svelte";
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext} from "svelte";
    import AssessmentOverlayCell from "./AssessmentOverlayCell.svelte";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import _ from "lodash";

    export let opts;

    const overlayData = getContext("overlayData");
    const widget = getContext("widget");
    const selectedDiagram = getContext("selectedDiagram");
    const selectedOverlay = getContext("selectedOverlay");
    const selectedAssessmentDefinition = getContext("selectedAssessmentDefinition");

    let overlayDataCall;


    function mkGlobalProps(data) {
        const maxCount = _
            .chain(data)
            .map(d => d.counts)
            .flatten()
            .map(d => d.count)
            .max()
            .value();

        return {maxCount};
    }


    function changeDefinition() {
        $selectedAssessmentDefinition = null;
    }


    function onSelect(ad) {
        $selectedOverlay = null;
        $selectedAssessmentDefinition = ad;

        const widgetParameters = {
            idSelectionOptions: opts
        }

        overlayDataCall = aggregateOverlayDiagramStore.findAppAssessmentsForDiagram(
            $selectedDiagram.id,
            ad.id,
            widgetParameters,
            true);
        $widget = {
            mkGlobalProps,
            overlay: AssessmentOverlayCell
        };
    }

    $: {
        $overlayData = $overlayDataCall?.data;
    }

</script>

{#if $selectedAssessmentDefinition}
    <h4>Showing: {$selectedAssessmentDefinition.name}</h4>

    <div class="help-block">
        <Markdown text={$selectedAssessmentDefinition.description}/>
    </div>
    <button class="btn btn-skinny" on:click={changeDefinition}>
        Change assessment
    </button>
{:else}
    <AssessmentDefinitionPicker {onSelect}
                                selectionFilter={ad => ad.entityKind === 'APPLICATION'}/>
{/if}

{#if $overlayDataCall?.status === 'loading'}
    <h4>
        Loading
        <Icon name="refresh" spin="true"/>
    </h4>
{/if}
