<script>

    import Icon from "../Icon.svelte";
    import EntitySearchSelector from "../EntitySearchSelector.svelte";

    export let onSelect = () => console.log("Selecting app group");
    export let selectionFilter = () => true;

    function onSelectGroup(e) {

        if (e.detail == null) {
            return;
        }
        return onSelect(e.detail);
    }


    $: appGroupSelectionFilter = (d) => {
        if (d === null) {
            return true;
        } else {
            return selectionFilter(d);
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
                      selectionFilter={appGroupSelectionFilter}>
</EntitySearchSelector>