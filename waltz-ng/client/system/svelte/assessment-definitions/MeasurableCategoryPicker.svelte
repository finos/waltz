<script>
    import _ from "lodash";
    import {measurableCategoryStore} from "../../../svelte-stores/measurable-category-store";

    const categoryCall = measurableCategoryStore.findAll();

    export let disabled = false;
    export let value = null;

    let categories = [];

    $: categories = _.sortBy($categoryCall?.data || [], d => d.name);
</script>


{#if $$slots.label}
    <label for="categories">
        <slot name="label"/>
    </label>
{/if}
<select id="categories"
        {disabled}
        bind:value>
    {#each categories as c}
        <option value={c.id}>
            {c.name}
        </option>
    {/each}
</select>

{#if $$slots.help}
    <div class="help-block">
        <slot name="help"/>
    </div>
{/if}

<style>
    label {
        display: block;
    }
</style>