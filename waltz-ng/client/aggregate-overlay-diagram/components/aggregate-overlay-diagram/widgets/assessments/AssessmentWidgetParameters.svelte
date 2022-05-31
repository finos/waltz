<script>
    import AssessmentDefinitionPicker
        from "../../../../../report-grid/components/svelte/pickers/AssessmentDefinitionPicker.svelte";
    import Markdown from "../../../../../common/svelte/Markdown.svelte";
    import {aggregateOverlayDiagramStore} from "../../../../../svelte-stores/aggregate-overlay-diagram-store";
    import {getContext} from "svelte";
    import AssessmentOverlayCell from "./AssessmentOverlayCell.svelte";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import _ from "lodash";

    const widget = getContext("widget");
    const selectedOverlay = getContext("selectedOverlay");
    const selectedAssessmentDefinition = getContext("selectedAssessmentDefinition");
    const remoteMethod = getContext("remoteMethod");
    const overlayDataCall = getContext("overlayDataCall");
    const widgetParameters = getContext("widgetParameters");

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

        $remoteMethod = aggregateOverlayDiagramStore.findAppAssessmentsForDiagram;

        $widgetParameters = {
            assessmentDefinitionId: ad.id
        }

        $selectedOverlay = null;

        $widget = {
            mkGlobalProps,
            overlay: AssessmentOverlayCell
        };
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
