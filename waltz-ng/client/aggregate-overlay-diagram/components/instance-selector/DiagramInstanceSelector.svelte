<script>

    import {createEventDispatcher, getContext} from "svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import _ from "lodash";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";


    export let instances = [];

    let selectedDiagram = getContext("selectedDiagram");


</script>

<h4>Selected: {$selectedDiagram?.name}</h4>
{#if !_.isEmpty(instances)}
    <p>Select an instance from the list below to see callouts</p>
    <ul>
        {#each instances as instance}
            <li>
                <EntityLink ref={instance}/>
                <span class="text-muted">
                    <LastEdited entity={instance}/>
                </span>
            </li>
        {/each}
    </ul>
{:else}
    <p>There are no instances of this diagram at this vantage point, would you like to
        <button class="btn btn-skinny">create one</button>
        ?
    </p>
{/if}

<style>
    ul {
        padding: 0.2em 0 0 0;
        margin: 0 0 0 0;
        list-style: none;
    }

    li {
        padding-top: 0.2em;
    }

</style>