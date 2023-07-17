<script>


    import _ from "lodash";
    import {groups, selectedGroup} from "../diagram-builder-store";
    import {mkGroup} from "../diagram-builder-utils";
    import EntityPicker from "../../../report-grid/components/svelte/pickers/EntityPicker.svelte";
    import {sameRef} from "../../../common/entity-utils";
    import {createEventDispatcher} from "svelte";
    import DropdownPicker
        from "../../../report-grid/components/svelte/column-definition-edit-panel/DropdownPicker.svelte";
    import {entity} from "../../../common/services/enums/entity";

    const dispatch = createEventDispatcher();

    let itemKind = _.get($selectedGroup, ["data", "kind"], null);

    let editingItemSize = false;

    let working = {
        itemWidth: $selectedGroup.props.minWidth,
        itemHeight: $selectedGroup.props.minHeight
    }

    const entityList = [
        entity.MEASURABLE,
        entity.DATA_TYPE,
        entity.PERSON,
    ];

    function selectItem(entity) {
        const groupNumber = _.size($groups) + 1;
        const newGroup = mkGroup(entity.name, groupNumber, $selectedGroup.id, groupNumber, $selectedGroup.props, entity)
        $groups = _.concat($groups, newGroup);
    }

    function deselectItem(entity) {
        $groups = _.reject($groups, d => d.parentId === $selectedGroup.id && sameRef(d.data, entity));
    }

    $: alreadyAddedFilter = (entity) => {
        return !_.some($groups, d => d.parentId === $selectedGroup.id && d.data && sameRef(d.data, entity));
    }

    function updateItemSizes() {
        const childGroups = _
            .chain($groups)
            .filter(d => d.parentId === $selectedGroup.id)
            .map(d => {
                console.log({working});
                const updatedProps = Object.assign({}, d.props, {minWidth: working.itemWidth, minHeight: working.itemHeight});

                return Object.assign({}, d, {props: updatedProps});
            })
            .value();

        console.log({childGroups});
        const withoutGroup = _.reject($groups, d => d.parentId === $selectedGroup.id);

        $groups = _.concat(withoutGroup, ...childGroups);
    }

    $: console.log({working});

    function cancel() {
        dispatch("cancel");
    }

</script>


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

<hr>

{#if editingItemSize}
    <label for="item-width">Item Width: {working.itemWidth}</label>
    <input id="item-width" type="range" min="5" max="20" bind:value={working.itemWidth}>
    <div class="help-block">
        This will override the minimum width of all items in this group. Defaults to minimum width of its parent.
    </div>
    <label for="item-height">Item Height: {working.itemHeight}</label>
    <input id="item-height" type="range" min="2" max="20" bind:value={working.itemHeight}>
    <div class="help-block">
        This will override the minimum height of all items in this group. Defaults to minimum height of its parent.
    </div>
    <button class="btn btn-default"
            on:click={updateItemSizes}>
        Save
    </button>
    <button class="btn btn-default"
            on:click={() => editingItemSize = false}>
        Cancel
    </button>
    <hr>
{/if}


<div>
    <button class="btn btn-default"
            on:click={() => editingItemSize = true}>
        Update Item Size
    </button>
    <button class="btn btn-default"
            on:click={cancel}>
        Cancel
    </button>
</div>

