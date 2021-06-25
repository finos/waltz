<script>
    import _ from "lodash";
    import GroupSelectorPanel from "./GroupSelectorPanel.svelte";
    import {createEventDispatcher} from "svelte";
    import {applicationStore} from "../../../../svelte-stores/application-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {colorSchemes, determineFillAndSymbol} from "./group-utils";
    import overlay from "../store/overlay";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import ColorPicker from "../../../../system/svelte/ratings-schemes/ColorPicker.svelte";
    import {measurableCategoryAlignmentViewStore} from "../../../../svelte-stores/measurable-category-alignment-view-store";
    import {mkRef} from "../../../../common/entity-utils";
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import OverlayGlyph from "./OverlayGlyph.svelte";

    export let group;
    export let overlays;
    export let diagramId;

    let workingOverlay;
    let relatedAppIds = [];
    let showColorPicker = false

    const Modes = {
        ADD_OVERLAY: "ADD_OVERLAY",
        SELECT_MEASURABLE: "SELECT_MEASURABLE",
        SELECT_APP_GROUP: "SELECT_APP_GROUP"
    }
    let activeMode = Modes.SELECT_MEASURABLE

    $: measurableAlignmentCall = measurableCategoryAlignmentViewStore
        .findAlignmentsByAppSelectorRoute(mkSelectionOptions(mkRef('FLOW_DIAGRAM', diagramId)));
    $: alignments = $measurableAlignmentCall.data;

    const dispatch = createEventDispatcher();

    function cancel() {
        workingOverlay = null;
        activeMode = Modes.SELECT_MEASURABLE;
    }

    function submit() {
        dispatch("cancel");
    }

    function selectOverlay(e) {
        workingOverlay = e.detail;
        activeMode = Modes.ADD_OVERLAY;
    }

    function getNewOverlay(overlay) {
        return Object.assign(
            {},
            determineFillAndSymbol(overlays),
            {entityReference: overlay, kind: 'OVERLAY'});
    }

    function addOverlay() {
        const overlayToAdd = Object.assign(
            {},
            newOverlay,
            {
                groupRef: group.id,
                applicationIds: _.map(relatedAppIds, d => d.id)
            });
        overlay.addOverlay(overlayToAdd)
        cancel();
        submit();
    }

    function selectColor(e) {
        newOverlay = Object.assign({}, newOverlay, {fill: e.detail});
        showColorPicker = false;
    }

    $: relatedAppsCall = workingOverlay && applicationStore.findBySelector(mkSelectionOptions(workingOverlay));
    $: relatedAppIds = $relatedAppsCall?.data || [];
    $: newOverlay = getNewOverlay(workingOverlay);

</script>


<div>
    {#if activeMode === Modes.SELECT_MEASURABLE && alignments}
        <h4>Adding measurable overlay for {group.data.name}:</h4>
        <GroupSelectorPanel on:select={selectOverlay} {alignments}/>
        <button on:click={() => activeMode = Modes.SELECT_APP_GROUP}
                class="btn btn-skinny">
            ...or add application group overlay
        </button>
    {:else if activeMode === Modes.SELECT_APP_GROUP}
        <h4>Adding application group overlay for {group.data.name}:</h4>
        <EntitySearchSelector on:select={selectOverlay}
                              placeholder="Search for app group"
                              entityKinds={['APP_GROUP']}>
        </EntitySearchSelector>
        <button on:click={() => activeMode = Modes.SELECT_MEASURABLE}
                class="btn btn-skinny">
            ...or add measurable overlay
        </button>
    {:else}
        <div style="padding-bottom: 1em">
            <strong>{newOverlay.entityReference.name}</strong>
            {#if !showColorPicker}
                <span>
                    <OverlayGlyph overlay={newOverlay}/>
                    <button class="btn btn-skinny" on:click={() => showColorPicker = true}>
                        <Icon name="pencil"/>Edit Colour
                    </button>
                </span>
                <div>
                    <button class="btn btn-skinny"
                            on:click={() => addOverlay()}>
                        Add
                    </button>
                    |
                    <button class="btn btn-skinny"
                            on:click={() => newOverlay = getNewOverlay(workingOverlay)}>
                        Refresh Icon
                    </button>
                    |
                    <button class="btn btn-skinny"
                            on:click={cancel}>
                        Cancel
                    </button>
                </div>
            {:else }
                <ColorPicker predefinedColors={_.map(colorSchemes, d => d.fill)}
                             startColor={newOverlay.fill}
                             on:select={selectColor}/>
                <button class="btn btn-skinny" on:click={() => showColorPicker = false}>Cancel</button>
            {/if}
        </div>
    {/if}
</div>

<style>
</style>