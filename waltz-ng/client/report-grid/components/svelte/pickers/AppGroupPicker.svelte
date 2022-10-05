<script>

    import Icon from "../../../../common/svelte/Icon.svelte";
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import {mkReportGridFixedColumnRef} from "../report-grid-utils";

    export let onSelect = () => console.log("Selecting app group");
    export let selectionFilter = () => true;

    function onSelectGroup(e) {

        if (e.detail == null) {
            return;
        }
        onSelect(mkReportGridFixedColumnRef(e.detail));
    }


    $: appGroupSelectionFilter = (d) => {
        if (d === null) {
            return true;
        } else {
            return selectionFilter(mkReportGridFixedColumnRef(d));
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