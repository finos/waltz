<script>

    import EntitySelector from "./EntitySelector.svelte";
    import _ from "lodash";

    let selectedEntities = [];

    function onSelect(d) {
        selectedEntities = _.concat(selectedEntities, d);
    }

    function clearSelectedEntity(d) {
        selectedEntities = _.without(selectedEntities, d);
    }

    let alreadyExists;

    $: alreadyExists = (d) => {
        console.log({selectedEntities, d});
        return !_.includes(selectedEntities, d)
    }

    console.log({selectedEntities});

</script>
<p>Selected:</p>
<ul>
    {#each selectedEntities as entity}
        <li>{entity.name}
            <button class="btn btn-skinny small"
                    on:click={() => clearSelectedEntity(entity)}>
                Clear
            </button></li>
    {/each}
</ul>

<hr>

<EntitySelector onSelect={onSelect}
                selectionFilter={alreadyExists}/>