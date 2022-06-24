<script>
    import {buildHierarchies} from "../../../common/hierarchy-utils";
    import OverlayDiagramBuilder from "./OverlayDiagramBuilder.svelte";
    import {mkChunks} from "../../../common/list-utils";

    export let categories;
    export let measurables;

    let selectedCategory = null;
    let groups = [];
    let cellMappings = [];

    $: groups = selectedCategory
        ? _.chain(measurables)
            .filter(m => m.categoryId === selectedCategory.id)
            .thru(buildHierarchies)
            .map(l1 => ({
                id: l1.externalId,
                name: l1.name,
                rows: mkChunks(l1.children.map(l2 => ({name: l2.name, id: l2.externalId, waltzId: l2.id})), 4)
            }))
            .value()
        : [];


    $: cellMappings = _
            .chain(groups)
            .flatMap(g => g.rows)
            .flatMap()
            .map(cell => `INSERT INTO aggregate_overlay_diagram_cell_data (diagram_id, cell_external_id, related_entity_kind, related_entity_id) VALUES (:id, '${cell.id}', 'MEASURABLE', ${cell.waltzId});`)
            .join("\n")
            .value();

    $: console.log({categories, measurables, groups})

</script>

<label>
    Category:
    <select bind:value={selectedCategory}
            class="form-control">
        {#each categories || [] as category}
            <option value={category}>{category.name}</option>
        {/each}
    </select>
</label>

{#if groups.length > 0}
    <OverlayDiagramBuilder config={groups}/>

    <pre>{cellMappings}</pre>
{/if}
