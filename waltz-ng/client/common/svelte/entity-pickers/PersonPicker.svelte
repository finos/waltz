<script>

    import Icon from "../Icon.svelte";
    import EntitySearchSelector from "../EntitySearchSelector.svelte";

    export let onSelect = () => console.log("Selecting person");
    export let selectionFilter = () => true;

    function onSelectPerson(e) {

        if (e.detail == null) {
            return;
        }
        return onSelect(e.detail);
    }


    $: personSelectionFilter = (d) => {
        if (d === null) {
            return true;
        } else {
            return selectionFilter(d);
        }
    }

</script>

<div class="help-block small">
    <Icon name="info-circle"/>
    Select a person using the search below.
</div>
<br>
<EntitySearchSelector on:select={onSelectPerson}
                      placeholder="Search for a person"
                      entityKinds={['PERSON']}
                      selectionFilter={personSelectionFilter}>
</EntitySearchSelector>