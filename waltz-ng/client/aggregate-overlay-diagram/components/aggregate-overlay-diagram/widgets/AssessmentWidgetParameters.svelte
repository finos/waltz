<script>
    import AssessmentDefinitionPicker from "../../../../report-grid/components/svelte/pickers/AssessmentDefinitionPicker.svelte";
    import Markdown from "../../../../common/svelte/Markdown.svelte";
    import {aggregateOverlayDiagramStore} from "../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext} from "svelte";
    import BulkAssessmentWidget from "./BulkAssessmentWidget.svelte";

    export let opts;

    const overlayData = getContext("overlayData");
    const widget = getContext("widget");
    const selectedDiagram = getContext("selectedDiagram");

    let selectedDefinition;
    let overlayDataCall;

    function onSelect(ad) {
        selectedDefinition = ad;
        overlayDataCall = aggregateOverlayDiagramStore.findAppAssessmentsForDiagram(
            $selectedDiagram.id,
            ad.id,
            opts,
            true);
        $widget = BulkAssessmentWidget;
    }

    $: {
        $overlayData = $overlayDataCall?.data;
    }

</script>

{#if selectedDefinition}
    <h4>Showing: {selectedDefinition.name}</h4>

    <div class="help-block">
        <Markdown text={selectedDefinition.description}/>
    </div>

    <h3>{$overlayData?.length || '??'}</h3>
{:else}
    <AssessmentDefinitionPicker {onSelect}
                                selectionFilter={ad => ad.entityKind === 'APPLICATION'}/>
{/if}
