<script>

    import _ from "lodash";
    import {diagramService} from "../entity-diagram-store";
    import {mkGroup} from "../entity-diagram-utils";
    import EntityPicker from "../../../../report-grid/components/svelte/pickers/EntityPicker.svelte";
    import {sameRef, toEntityRef} from "../../../../common/entity-utils";
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

    const dispatch = createEventDispatcher();

    const entityList = [
        entity.MEASURABLE,
        entity.DATA_TYPE,
        entity.PERSON,
    ];

    const EditModes = {
        ADD: "ADD",
        SIZE: "SIZE",
        COLOR: "COLOR"
    }

    let activeMode = EditModes.ADD;
    let editingItemSize = false;

    let working = {}

    const {selectedGroup, groups, addGroup, updateChildren} = diagramService;

    let itemKind = _.get($selectedGroup, ["data", "kind"], null);

    function selectItem(entity) {
        const groupNumber = _.size($groups) + 1;
        const id = generateUUID();
        const newGroup = mkGroup(entity.name, id, $selectedGroup.id, groupNumber, $selectedGroup.props)
        addGroup(newGroup, entity);
    }

    function determineStore(ref) {
        if (_.isEmpty(ref)) {
            console.log(`Cannot load children where the parent doesn't have data associated`);
        } else {
            switch (ref.kind) {
                case entity.MEASURABLE.key:
                    return measurableStore.findByParentId(ref.id);
                case entity.DATA_TYPE.key:
                    return dataTypeStore.findByParentId(ref.id);
                case entity.PERSON.key:
                    return personStore.findDirectsForPersonIds([ref.id]);
                default:
                    console.log(`Cannot load children for entity kind: ${ref.kind}`);
            }
        }
    }

    function addChildren() {
        const existingChildren = _.filter($groups, d => d.parentId === $selectedGroup.id);
        _.chain(directChildren)
            .reject(child => _.some(existingChildren, d => sameRef(d.data.entityReference, child)))
            .forEach(child => selectItem(child))
            .value();
    }

    function deselectItem(entity) {
        $groups = _.reject($groups, d => d.parentId === $selectedGroup.id && sameRef(d.data.entityReference, entity));
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

    $: fetchChildrenStore = determineStore($selectedGroup.data?.entityReference);
    $: directChildren = $fetchChildrenStore?.data || [];

    $: console.log({backgroundColors, titleColors, gs: $groups})

</script>


{#if activeMode === EditModes.ADD}
    <DropdownPicker items={entityList}
                    onSelect={(item) => itemKind = item.key}
                    defaultMessage="Select an item kind for this group"
                    selectedItem={_.find(entityList, d => d.key === itemKind)}/>
    <div class="help-block">
        Pick a type of entity to add or remove from this group, by default it will be the kind of the parent.
    </div>

    {#if itemKind}
        <EntityPicker entityKind={itemKind}
                      onSelect={selectItem}
                      onDeselect={deselectItem}
                      selectionFilter={alreadyAddedFilter}/>
    {/if}

    <div class="controls">
        <button class="btn btn-default"
                on:click={updateSize}>
            Update Item Size
        </button>
        <button class="btn btn-default"
                on:click={updateColors}>
            Update Item Colors
        </button>
        <button class="btn btn-default"
                on:click={addChildren}>
            Add Direct Children
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
        This will override the minimum width of all items in this group. Defaults to minimum width of its parent.
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
