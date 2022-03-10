<script>

    import Icon from "../../../../common/svelte/Icon.svelte";
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";

    export let onSelect = () => console.log("Selecting involvement kind");
    export let selectionFilter = () => true;

    function onSelectGroup(e) {

        if (e.detail == null) {
            return;
        }

        const column = {
            columnEntityId: e.detail.id,
            columnEntityKind: e.detail.kind,
            entityFieldReference: null,
            columnName: e.detail.name,
            displayName: null
        }

        onSelect(column);
    }


    $: canBeAdded = (d) => {

        if (d === null) {
            return true;
        } else {

            const column = {
                columnEntityId: d.id,
                columnEntityKind: d.kind,
                entityFieldReference: null,
                columnName: d.name,
                displayName: null
            }

            return selectionFilter(column);
        }
    }

</script>

<div class="help-block small">
    <Icon name="info-circle"/>
    Select an app group using the search below.
</div>
<br>
<EntitySearchSelector on:select={onSelectGroup}
                      placeholder="Search for app group"
                      entityKinds={['APP_GROUP']}
                      selectionFilter={canBeAdded}>
</EntitySearchSelector>