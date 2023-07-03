<script>


    import _ from "lodash";
    import {groups, selectedGroup} from "../diagram-builder-store";
    import {FlexDirections, mkGroup} from "../diagram-builder-utils";
    import DropdownPicker
        from "../../../report-grid/components/svelte/column-definition-edit-panel/DropdownPicker.svelte";
    import {entity} from "../../../common/services/enums/entity";
    import {createEventDispatcher} from "svelte";

    let working = Object.assign({}, $selectedGroup);

    const dispatch = createEventDispatcher();

    let entityList = [
        entity.MEASURABLE,
        entity.DATA_TYPE,
        entity.PERSON,
    ];

    function editGroupKind(d) {
        working.itemKind = d.key;
        console.log({gs: $groups});
    }

    function editTitle(d) {
        const group = _.find($groups, d => d.id === $selectedGroup.id);

        group.title = d?.key || null;

        const withoutGroup = _.reject($groups, d => d.id === $selectedGroup.id);
        $groups = _.concat(withoutGroup, group);
        $selectedGroup = group;
        console.log({gs: $groups});
    }

    function saveGroup() {
        dispatch("save", working);
    }

    function cancel() {
        dispatch("cancel");
    }

</script>


<input class="form-control"
       id="title"
       maxlength="255"
       placeholder="New password"
       bind:value={working.title}/>
<div class="help-block">
    The title of this group.
</div>

<DropdownPicker items={entityList}
                onSelect={editGroupKind}
                defaultMessage="Select an item kind for this group"
                selectedItem={_.find(entityList, d => d.key === working.itemKind)}/>
<div class="help-block">
    Items in this group will be of this entity kind.
</div>

<button class="btn btn-default"
        on:click={saveGroup}>
    Save
</button>
<button class="btn btn-default"
        on:click={cancel}>
    Cancel
</button>


