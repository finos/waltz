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

    // $: console.log({selectedCategory, overlayGroups, first, selectedCategoryAlignments});


    function selectGroup(e) {
        dispatch("select", e.detail);
    }

</script>

{#if selectCategory}
    <div class="form-group">
        <label for="selectedCategory">
            Category:
        </label>
        <select id="selectedCategory"
                bind:value={selectedCategory}
                class="form-control">
            {#each alignments as alignment}
                <option value={alignment.categoryReference}>
                    {alignment.categoryReference.name}
                </option>
            {/each}
        </select>
    </div>
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

</style>