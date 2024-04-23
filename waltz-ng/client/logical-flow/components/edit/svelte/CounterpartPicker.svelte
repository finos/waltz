<script>

    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import {entity} from "../../../../common/services/enums/entity";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {onMount} from "svelte";

    export let onSelect = (item) => console.log("Selected: " + JSON.stringify(item));

    onMount(() => {
        selectedKinds = _.map(possibleSearchKinds, d => d.key);
    })

    let selectedKinds = [];
    const possibleSearchKinds = [entity.APPLICATION, entity.ACTOR, entity.END_USER_APPLICATION];

    function selectCounterpart(e) {
        return onSelect(e.detail);
    }

</script>

<ul>
    {#each possibleSearchKinds as entityKind}
        <li class="waltz-visibility-parent">
            <label class="clickable check-label">
                <input type="checkbox"
                       name="selectedKinds"
                       bind:group={selectedKinds}
                       value={entityKind.key}>
                {entityKind.name}
                <span class="waltz-visibility-child-30">
                    <Icon name={entityKind.icon}/>
                </span>
            </label>
        </li>
    {/each}
</ul>

<div style="width:100%; padding-top: 1em">
    <EntitySearchSelector entityKinds={selectedKinds}
                          on:select={selectCounterpart}/>
</div>

<div class="help-text small">Search for the entity using the search box, you can filter limit the results to a particular entity kind using the filters above</div>

<style>

    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    .check-label {
        font-weight: normal;
    }

</style>