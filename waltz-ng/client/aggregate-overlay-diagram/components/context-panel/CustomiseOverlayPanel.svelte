<script>

    import WidgetSelector from "../aggregate-overlay-diagram/WidgetSelector.svelte";
    import CreatePresetPanel from "../aggregate-overlay-diagram/presets/CreatePresetPanel.svelte";
    import PresetSelector from "../aggregate-overlay-diagram/presets/PresetSelector.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import {getContext} from "svelte";
    import _ from "lodash";
    import NoData from "../../../common/svelte/NoData.svelte";
    import FilterPanel from "../filter-selector/FilterPanel.svelte";

    export let primaryEntityRef;

    const diagramPresets = getContext("diagramPresets");
    const filterParameters = getContext("filterParameters");
    const focusWidget = getContext("focusWidget");
    const selectionOptions = getContext("selectionOptions");

    const Modes = {
        PRESET: "PRESET",
        CUSTOM: "CUSTOM",
        CREATE_PRESET: "CREATE_PRESET"
    }

    $: $selectionOptions = mkSelectionOptions(primaryEntityRef);

    let activeMode = Modes.CUSTOM;

</script>


{#if activeMode === Modes.PRESET}
    <div class="help-block">
        <Icon name="info-circle"/>
        Select a preset from the list below, or alternatively create a custom view
        from the overlay and filter pickers.
    </div>
    <PresetSelector/>
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.CUSTOM}>
        <Icon name="plus"/>
        Create custom overlay
    </button>
{:else if activeMode === Modes.CUSTOM}
    {#if !_.isEmpty($diagramPresets)}
        <NoData type="info">
            There are
            <button class="btn btn-skinny"
                    on:click={() => activeMode = Modes.PRESET}>
                {_.size($diagramPresets)} preset overlays
            </button>
            configured for this diagram
        </NoData>
        <br>
    {/if}
    <WidgetSelector {primaryEntityRef}/>
    <hr>
    <FilterPanel existingFilters={!_.isEmpty($filterParameters)}/>
    <div>
        <br>
        <button class="btn btn-skinny"
                title={_.isNull($focusWidget) ? "You must select an overlay to save a preset" : ""}
                disabled={_.isNull($focusWidget)}
                on:click={() => activeMode = Modes.CREATE_PRESET}>
            <Icon name="save"/>
            Save as preset
        </button>
    </div>
{:else if activeMode === Modes.CREATE_PRESET}
    <CreatePresetPanel on:cancel={() => activeMode = Modes.CUSTOM}/>
{/if}