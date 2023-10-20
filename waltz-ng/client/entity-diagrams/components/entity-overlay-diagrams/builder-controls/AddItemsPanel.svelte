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

    const entityList = [
        entity.MEASURABLE,
        entity.DATA_TYPE,
        entity.PERSON,
    ];

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

    function deselectItem(entity) {
        $groups = _.reject($groups, d => d.parentId === $selectedGroup.id && sameRef(d.data.entityReference, entity));
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

    function createGroup() {
        const groupNumber = _.size($groups) + 1;
        const id = generateUUID();
        const newGroup = mkGroup("Group " + groupNumber.toString(), id, $selectedGroup.id, groupNumber, $selectedGroup.props)
        addGroup(newGroup);
    }

    function addChildren() {
        const existingChildren = _.filter($groups, d => d.parentId === $selectedGroup.id);
        _.chain(directChildren)
            .reject(child => _.some(existingChildren, d => sameRef(d.data.entityReference, child)))
            .forEach(child => selectItem(child))
            .value();
    }

    function cancel() {
        dispatch("cancel");
    }

    $: alreadyAddedFilter = (entity) => {
        return !_.some($groups, d => d.parentId === $selectedGroup.id && d.data && sameRef(d.data.entityReference, entity));
    }

    $: fetchChildrenStore = determineStore($selectedGroup.data?.entityReference);

    $: directChildren = $fetchChildrenStore?.data || [];

</script>

<div>

    <h4>Adding items to: {$selectedGroup.title}</h4>
    <div class="help-block">
        <Icon name="info-circle"/> Use the controls below to add items to this group.
        These can be empty groups (useful for styling or adjusting the layout of the diagram), or groups backed by data e.g. viewpoint, data type or person.
        If the selected group is backed by a waltz entity you can quickly add all of it's direct children in one go.
    </div>

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
                on:click={createGroup}>
            Add Empty Group
        </button>
        <button class="btn btn-default"
                disabled={_.isEmpty($selectedGroup.data)}
                title={_.isEmpty($selectedGroup.data) ? "There is no waltz entity backing this selected group so direct children cannot be added" : ""}
                on:click={addChildren}>
            Add Direct Children
        </button>
        <button class="btn btn-default"
                on:click={cancel}>
            Cancel
        </button>
    </div>

</div>


<style>

    .controls {
        display: flex;
        flex-wrap: wrap;
        align-content: flex-start;
        align-items: flex-start;
        gap: 0.5em;
    }

</style>
