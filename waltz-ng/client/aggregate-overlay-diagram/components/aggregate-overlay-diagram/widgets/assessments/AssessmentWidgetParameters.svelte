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

    let selectedDefinition;
    let overlayDataCall;


    function mkGlobalProps(data) {
        const maxCount = _
            .chain(data)
            .map(d => d.counts)
            .flatten()
            .map(d => d.count)
            .max()
            .value();

        return { maxCount };
    }


    function onSelect(ad) {
        $selectedOverlay = null;
        selectedDefinition = ad;
        overlayDataCall = aggregateOverlayDiagramStore.findAppAssessmentsForDiagram(
            $selectedDiagram.id,
            ad.id,
            opts,
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

{#if selectedDefinition}
    <h4>Showing: {selectedDefinition.name}</h4>

    <div class="help-block">
        <Markdown text={selectedDefinition.description}/>
    </div>
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
