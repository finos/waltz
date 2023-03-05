<script>
    import { createEventDispatcher } from 'svelte';
    import EntitySearchSelector from "../../../../../common/svelte/EntitySearchSelector.svelte";
    import {likelyPeople} from "../builderStore";
    import Icon from "../../../../../common/svelte/Icon.svelte";

    export let params;

    const dispatch = createEventDispatcher();
</script>


<h3>{params.message}</h3>

<label for="title">
    Title
</label>
<input id="title"
       type="text"
       bind:value={params.data.title}
       placeholder="Title"/>
<div class="help-block small">
    The title of the person, e.g. Chief Information Officer
</div>

<label style="padding-top: 1.2em;"
       for="leaderPerson">
    Person
</label>

<br>
{#if params.data.person}
    {params.data.person.name}
    <button class="btn-skinny"
            on:click={() => params.data.person = null}>
        <Icon name="times"/>
        Change
    </button>
{:else}
    People reporting leaders:
    <ul>
        {#each $likelyPeople as person}
            <li>
                <button class="btn-skinny"
                        on:click={() =>  params.data.person = person}>
                    {person.name}
                </button>
            </li>
        {:else}
            <li class="no-likely-people">Cannot determine likely people until one or more leaders are set</li>
        {/each}
    </ul>

    Or pick anyone:
    <div id="leaderPerson">
        <EntitySearchSelector entityKinds={["PERSON"]}
                              on:select={evt => params.data.person = evt.detail}/>
    </div>
    <div class="help-block small">
        The person holding this title
    </div>
{/if}

<hr>

<button class="btn btn-primary"
        on:click={() => dispatch("confirm", params)}>
    Save
</button>
<button class="btn-skinny"
        on:click={() => dispatch("cancel")}>
    Cancel
</button>


<style>
    #title {
        width: 100%;
    }

    .no-likely-people {
        color: #af8433;
    }
</style>
