<script>


    import _ from "lodash";
    import {backgroundColors, groups, selectedGroup, titleColors} from "../diagram-builder-store";
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

        if (!_.includes($titleColors, evt.detail)) {
            $titleColors = _.concat($titleColors, evt.detail);
        }
        working.props.titleColor = evt.detail;
    }

    function updateContentColor(evt) {
        if (!_.includes($backgroundColors, evt.detail)) {
            $backgroundColors = _.concat($backgroundColors, evt.detail);
        }
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
    <input id="bucketSize" type="range" min="1" max="10" bind:value={working.props.bucketSize}>
    <div class="help-block">
        The number of items to be displayed in the group before wrapping occurs
    </div>

    <label for="proportion">Proportion: {working.props.proportion}</label>
    <input id="proportion" type="range" min="0" max="10" bind:value={working.props.proportion}>
    <div class="help-block">
        The proportion of free space this group will occupy relative to its siblings
    </div>

    <label for="item-width">Item Width: {working.props.minWidth}</label>
    <input id="item-width" type="range" min="2" max="20" bind:value={working.props.minWidth}>
    <div class="help-block">
        The minimum width of an item, if more space is available it will expand to use it
    </div>

    <label for="item-height">Item Height: {working.props.minHeight}</label>
    <input type="range" id="item-height" min="2" max="20" bind:value={working.props.minHeight}>
    <div class="help-block">
        The minimum height of an item, if more space is available it will expand to use it
    </div>

    <label for="title-font-size">Title Font Size: {working.props.titleFontSize}</label>
    <input type="range" id="title-font-size" min="0.1" max="2" step="0.1" bind:value={working.props.titleFontSize}>
    <div class="help-block">
        The font size of the title for an item or group.
    </div>

    <label for="content-font-size">Content Font Size: {working.props.contentFontSize}</label>
    <input type="range" id="content-font-size" min="0.1" max="1" step="0.1" bind:value={working.props.contentFontSize}>
    <div class="help-block">
        The font size of all text within the content box of an item.
    </div>

    <label for="start-color">Title Color</label>
    <div id="start-color">
        <ColorPicker startColor={working.props.titleColor}
                     on:select={updateTitleColor}
                     predefinedColors={$titleColors}/>
    </div>
    <div class="help-block">
        Select a title color for this group from a predefined color or use the custom button to pick out a custom color.
    </div>

    <label for="content-color">Content Color</label>
    <div id="content-color">
        <ColorPicker startColor={working.props.contentColor}
                     on:select={updateContentColor}
                     predefinedColors={$backgroundColors}/>
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


