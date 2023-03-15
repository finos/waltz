<script>
    import TaxonomyNavAidBuilder from "./TaxonomyNavAidBuilder.svelte";
    import {measurableStore} from "../../../svelte-stores/measurables";
    import {measurableCategoryStore} from "../../../svelte-stores/measurable-category-store";

    let catCall = measurableCategoryStore.findAll();
    let measurableCall = measurableStore.loadAll();

    let categories = [];
    let measurablesByCategory = {};
    let selectedMeasurables = [];
    let selectedCategory = null;

    $: categories = _.orderBy($catCall.data, d => d.name);
    $: measurablesByCategory = _.groupBy($measurableCall.data, d => d.categoryId);
    $: selectedMeasurables = measurablesByCategory[selectedCategory] || [];
</script>


<h4>Select category</h4>
<p class="help-block">
    First select the category you wish to draw.
</p>

<select bind:value={selectedCategory}>
    {#each categories as category}
        <option value={category.id}>
            {category.name}
        </option>
    {/each}
</select>

{#if selectedCategory}
    <h4>Navigation Aid</h4>
    <p class="help-block">
        You may use the basic controls to alter the dimensions etc.
        If you are happy with the results, grab the svg and insert it into the
        <code>svg_diagram</code> table with the column <code>group</code>
        set to <code>NAVAID.MEASURABLE.{selectedCategory}</code>. Example:
    </p>

    <code>
        INSERT INTO svg_diagram (name, "group", priority, description, svg, key_property, product)
        VALUES ('Some Title', 'NAVAID.MEASURABLE.{selectedCategory}', 1, 'Some desc', &lt;SVG goes here&gt;, ' ',
        'svg');
    </code>

    <hr>

    <TaxonomyNavAidBuilder taxonomy={selectedMeasurables}
                           link='measurable'/>
{/if}