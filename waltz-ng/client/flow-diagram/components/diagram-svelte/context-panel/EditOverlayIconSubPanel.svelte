<script>
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import {applicationStore} from "../../../../svelte-stores/application-store";
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {colorSchemes, mkDecoratorId} from "./group-utils";
    import overlay from "../store/overlay";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import ColorPicker from "../../../../system/svelte/ratings-schemes/ColorPicker.svelte";
    import OverlayGlyph from "./OverlayGlyph.svelte";
    import SymbolPicker from "../../../../system/svelte/ratings-schemes/SymbolPicker.svelte";
    import {symbolsByName} from "../flow-diagram-utils";

    export let group;
    export let selectedOverlay;

    let relatedAppIds = [];

    const Modes = {
        ADD_OVERLAY: "ADD_OVERLAY",
        EDIT_SYMBOL: "EDIT_SYMBOL",
        EDIT_COLOUR: "EDIT_COLOUR"
    }

    let activeMode = Modes.ADD_OVERLAY;

    const dispatch = createEventDispatcher();

    function cancel() {
        newOverlay = selectedOverlay;
        dispatch("cancel");
    }

    function saveOverlay() {
        const overlayToAdd = Object.assign(
            {},
            newOverlay,
            {
                groupRef: group.id,
                applicationIds: _.map(relatedAppIds, d => d.id)
            });

        overlay.addOverlay(overlayToAdd)
        cancel();
    }

    function selectColor(e) {
        newOverlay = Object.assign(
            {},
            newOverlay,
            {
                fill: e.detail,
                stroke: e.detail,
                decoratorId: mkDecoratorId(newOverlay.symbol, e.detail, e.detail)
            });

        activeMode = Modes.ADD_OVERLAY;
    }

    function selectSymbol(e) {
        newOverlay = Object.assign(
            {},
            newOverlay,
            {
                symbol: e.detail,
                decoratorId: mkDecoratorId(e.detail, newOverlay.fill, newOverlay.stroke)
            });
        activeMode = Modes.ADD_OVERLAY;
    }

    $: relatedAppsCall = newOverlay && applicationStore.findBySelector(mkSelectionOptions(newOverlay.entityReference));
    $: relatedAppIds = $relatedAppsCall?.data || [];

    let newOverlay = selectedOverlay;

</script>


<div>
    {#if activeMode === Modes.ADD_OVERLAY}
        <div style="padding-bottom: 1em">
            <strong>{newOverlay.entityReference.name}</strong>
            <OverlayGlyph overlay={newOverlay}/>
            <div>
                <ul>
                    <li>
                        <button class="btn btn-skinny"
                                on:click={() => activeMode = Modes.EDIT_COLOUR}>
                            <Icon name="pencil"/>Edit Colour
                        </button>
                    </li>
                   <li>
                       <button class="btn btn-skinny"
                               on:click={() => activeMode = Modes.EDIT_SYMBOL}
                                on:cancel={() => activeMode = Modes.ADD_OVERLAY}
                                on:submit={selectSymbol}>
                           <Icon name="pencil"/>Edit Symbol
                        </button>
                   </li>
                </ul>
            </div>
            <div style="padding-top: 1em">
                <button class="btn btn-skinny"
                        on:click={() => saveOverlay()}>
                    Save
                </button>
                |
                <button class="btn btn-skinny"
                        on:click={cancel}>
                    Cancel
                </button>
            </div>
        </div>
    {:else if activeMode === Modes.EDIT_COLOUR}
        <ColorPicker predefinedColors={_.map(colorSchemes, d => d.fill)}
                     startColor={newOverlay.fill}
                     on:select={selectColor}/>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD_OVERLAY}>
            Cancel
        </button>
    {:else if activeMode === Modes.EDIT_SYMBOL}
        <SymbolPicker color={newOverlay.fill}
                      symbolsByName={symbolsByName}
                      startSymbol={newOverlay.symbol}
                      on:select={selectSymbol}/>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD_OVERLAY}>
            Cancel
        </button>
    {/if}
</div>

<style>
    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
        padding-top: 0;
    }
</style>