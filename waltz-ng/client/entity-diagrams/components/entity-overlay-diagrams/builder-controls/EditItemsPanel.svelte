<script>

    import _ from "lodash";
    import {diagramService} from "../entity-diagram-store";
    import {mkGroup} from "../entity-diagram-utils";
    import EntityPicker from "../../../../report-grid/components/svelte/pickers/EntityPicker.svelte";
    import {sameRef} from "../../../../common/entity-utils";
    import {createEventDispatcher} from "svelte";
    import DropdownPicker
        from "../../../../report-grid/components/svelte/column-definition-edit-panel/DropdownPicker.svelte";
    import {entity} from "../../../../common/services/enums/entity";
    import {measurableStore} from "../../../../svelte-stores/measurables";
    import {dataTypeStore} from "../../../../svelte-stores/data-type-store";
    import {personStore} from "../../../../svelte-stores/person-store";
    import ColorPicker from "../../../../system/svelte/ratings-schemes/ColorPicker.svelte";
    import {backgroundColors, titleColors} from "../builder/diagram-builder-store";
    import {generateUUID} from "../../../../system/svelte/nav-aid-builder/custom/builderStore";
    import Icon from "../../../../common/svelte/Icon.svelte";

    const dispatch = createEventDispatcher();

    const EditModes = {
        VIEW: "VIEW",
        SIZE: "SIZE",
        COLOR: "COLOR"
    }

    let activeMode = EditModes.VIEW;
    let editingItemSize = false;

    let working = {}

    const {selectedGroup, groups, addGroup, updateChildren} = diagramService;

    function toggleItemTitleDisplay() {
        const childGroups = _
            .chain($groups)
            .filter(d => d.parentId === $selectedGroup.id)
            .map(d => {
                const updatedProps = Object.assign({}, d.props, {showTitle: !d.props.showTitle});

                return Object.assign({}, d, {props: updatedProps});
            })
            .value();
        updateChildren($selectedGroup.id, childGroups);
    }

    function toggleItemBorderDisplay() {
        const childGroups = _
            .chain($groups)
            .filter(d => d.parentId === $selectedGroup.id)
            .map(d => {
                const updatedProps = Object.assign({}, d.props, {showBorder: !d.props.showBorder});

                return Object.assign({}, d, {props: updatedProps});
            })
            .value();
        updateChildren($selectedGroup.id, childGroups);
    }

    function updateItemSizes() {
        const childGroups = _
            .chain($groups)
            .filter(d => d.parentId === $selectedGroup.id)
            .map(d => {
                const updatedProps = Object.assign({}, d.props, {minWidth: working.minWidth, minHeight: working.minHeight});
                return Object.assign({}, d, {props: updatedProps});
            })
            .value();

        updateChildren($selectedGroup.id, childGroups);
        activeMode = EditModes.ADD;
    }

    function updateItemColors() {
        const childGroups = _
            .chain($groups)
            .filter(d => d.parentId === $selectedGroup.id)
            .map(d => {
                const updatedProps = Object.assign({}, d.props, {titleColor: working.titleColor, contentColor: working.contentColor});
                return Object.assign({}, d, {props: updatedProps});
            })
            .value();

        updateChildren($selectedGroup.id, childGroups);
        activeMode = EditModes.ADD;
    }

    function updateTitleColor(evt) {
        working.titleColor = evt.detail;
    }

    function updateContentColor(evt) {
        working.contentColor = evt.detail;
    }

    function updateSize() {
        working = {
            minWidth: $selectedGroup.props.minWidth,
            minHeight: $selectedGroup.props.minHeight
        }
        activeMode = EditModes.SIZE;
    }

    function updateColors() {
        working = {
            titleColor: $selectedGroup.props.titleColor,
            contentColor: $selectedGroup.props.contentColor,
        }
        activeMode = EditModes.COLOR;
    }

    function cancel() {
        dispatch("cancel");
    }

    $: alreadyAddedFilter = (entity) => {
        return !_.some($groups, d => d.parentId === $selectedGroup.id && d.data && sameRef(d.data.entityReference, entity));
    }

</script>


{#if activeMode === EditModes.VIEW}
    <div class="help-block">
        <Icon name="info-circle"/> Use the controls below to update the properties of the direct children of this group.
    </div>
    <div class="controls">
        <button class="btn btn-default"
                on:click={() => toggleItemTitleDisplay()}>
            Toggle Title Display
        </button>
        <button class="btn btn-default"
                on:click={() => toggleItemBorderDisplay()}>
            Toggle Border Display
        </button>
        <button class="btn btn-default"
                on:click={updateSize}>
            Update Item Size
        </button>
        <button class="btn btn-default"
                on:click={updateColors}>
            Update Item Colors
        </button>
        <button class="btn btn-default"
                on:click={cancel}>
            Cancel
        </button>
    </div>

{:else if activeMode === EditModes.SIZE}

    <label for="item-width">Item Width: {working.minWidth}</label>
    <input id="item-width" type="range" min="2" max="20" bind:value={working.minWidth}>
    <div class="help-block">
        This will override the minimum width of all children of this group. Defaults to minimum width of its parent.
    </div>
    <label for="item-height">Item Height: {working.minHeight}</label>
    <input id="item-height" type="range" min="2" max="20" bind:value={working.minHeight}>
    <div class="help-block">
        This will override the minimum height of all items in this group. Defaults to minimum height of its parent.
    </div>

    <div>
        <button class="btn btn-default"
                on:click={updateItemSizes}>
            Save
        </button>
        <button class="btn btn-default"
                on:click={() => activeMode = EditModes.ADD}>
            Cancel
        </button>
    </div>

{:else if activeMode === EditModes.COLOR}

    <label for="start-color">Title Color</label>
    <div id="start-color">
        <ColorPicker startColor={working.titleColor}
                     on:select={updateTitleColor}
                     predefinedColors={$titleColors}/>
    </div>
    <div class="help-block">
        Select a title color for this group from a predefined color or use the custom button to pick out a custom color.
    </div>

    <label for="content-color">Content Color</label>
    <div id="content-color">
        <ColorPicker startColor={working.contentColor}
                     on:select={updateContentColor}
                     predefinedColors={$backgroundColors}/>
    </div>
    <div class="help-block">
        Select a background colour for this group from a predefined color or use the custom button to pick out a custom color.
    </div>

    <div>
        <button class="btn btn-default"
                on:click={updateItemColors}>
            Save
        </button>
        <button class="btn btn-default"
                on:click={() => activeMode = EditModes.ADD}>
            Cancel
        </button>
    </div>

{/if}


<style>

    .controls {
        display: flex;
        flex-wrap: wrap;
        align-content: flex-start;
        align-items: flex-start;
        gap: 0.5em;
    }

</style>
