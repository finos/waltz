<script>
    import {store} from "../diagram-model-store";
    import EntitySearchSelector from "../../../../common/svelte/EntitySearchSelector.svelte";
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import MeasurableAlignmentTreeSelector from "./MeasurableAlignmentTreeSelector.svelte";

    const dispatch = createEventDispatcher();

    let selectCategory = true;

    export let alignments;

    let selectedCategory;

    $: decoratorGroups = $store.model.groups;

    $: first = _.first(alignments);

    $: selectedCategory = selectedCategory || first?.categoryReference

    $: selectedCategoryAlignments = _.find(alignments, d => d.categoryReference === selectedCategory);

    $: console.log({selectedCategory, decoratorGroups, first, selectedCategoryAlignments});


    function selectGroup(e) {
        console.log("sleector panel", e);
        dispatch("select", e.detail);
    }

</script>

{#if selectCategory}
    {#each alignments as alignment}
        <label>
            <input type=radio
                   bind:group={selectedCategory}
                   value={alignment.categoryReference}>
            {alignment.categoryReference.name}
        </label>
    {/each}
    {#if selectedCategory}
    <MeasurableAlignmentTreeSelector alignments={selectedCategoryAlignments.alignments}
                            on:select={selectGroup}/>
    {/if}
{:else}
    <h4>Add a new group:</h4>
    <EntitySearchSelector on:select={selectGroup}
                          placeholder="Search for group"
                          entityKinds={['MEASURABLE', 'APP_GROUP']}>
    </EntitySearchSelector>
{/if}


<style>
    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
        padding-top: 0;
    }
</style>