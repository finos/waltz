<script>


    import _ from "lodash";
    import {groups, selectedGroup} from "../diagram-builder-store";
    import {createEventDispatcher, onMount} from "svelte";
    import ColorPicker from "../../../system/svelte/ratings-schemes/ColorPicker.svelte";
    import {defaultBgColors, defaultColors} from "../diagram-builder-utils";

    let working;

    onMount(() => {
        working = _.cloneDeep(_.find($groups, d => d.id === $selectedGroup.id));
    });

    const dispatch = createEventDispatcher();

    function saveGroup() {
        dispatch("save", working);
    }

    function cancel() {
        dispatch("cancel");
    }


    function updateTitleColor(evt) {
        working.props.titleColor = evt.detail;
    }

    function updateContentColor(evt) {
        working.props.contentColor = evt.detail;
    }

</script>


{#if working}
    <input class="form-control"
           id="title"
           maxlength="255"
           placeholder="New password"
           bind:value={working.title}/>
    <div class="help-block">
        The title of this group.
    </div>

    <label for="showTitle">Display Title:</label>
    <input type=checkbox
           id="showTitle"
           bind:checked={working.props.showTitle}>
    <div class="help-block">
        Show or hide the title of this group or item.
    </div>

    <label for="showBorder">Display Border:</label>
    <input type=checkbox
           id="showBorder"
           bind:checked={working.props.showBorder}>
    <div class="help-block">
        Show or hide the border of this group or item.
    </div>

    <label for="bucketSize">Bucket Size: {working.props.bucketSize}</label>
    <input id="bucketSize" type="range" min="1" max="5" bind:value={working.props.bucketSize}>
    <div class="help-block">
        The number of items to be displayed in the group before wrapping occurs
    </div>

    <label for="proportion">Proportion: {working.props.proportion}</label>
    <input id="proportion" type="range" min="0" max="5" bind:value={working.props.proportion}>
    <div class="help-block">
        The proportion of free space this group will occupy relative to its siblings
    </div>

    <label for="item-width">Item Width: {working.props.itemWidth}</label>
    <input id="item-width" type="range" min="5" max="20" bind:value={working.props.itemWidth}>
    <div class="help-block">
        The minimum width of an item, if more space is available it will expand to use it
    </div>

    <label for="item-height">Item Height: {working.props.itemHeight}</label>
    <input type="range" id="item-height" min="2" max="20" bind:value={working.props.itemHeight}>
    <div class="help-block">
        The minimum height of an item, if more space is available it will expand to use it
    </div>

    <label for="start-color">Title Color</label>
    <div id="start-color">
        <ColorPicker startColor={working.props.titleColor}
                     on:select={updateTitleColor}
                     predefinedColors={defaultColors}/>
    </div>
    <div class="help-block">
        Select a title color for this group from a predefined color or use the custom button to pick out a custom color.
    </div>

    <label for="content-color">Content Color</label>
    <div id="content-color">
        <ColorPicker startColor={working.props.contentColor}
                     on:select={updateContentColor}
                     predefinedColors={defaultBgColors}/>
    </div>
    <div class="help-block">
        Select a background colour for this group from a predefined color or use the custom button to pick out a custom color.
    </div>
{/if}

<button class="btn btn-default"
        on:click={saveGroup}>
    Save
</button>
<button class="btn btn-default"
        on:click={cancel}>
    Cancel
</button>


