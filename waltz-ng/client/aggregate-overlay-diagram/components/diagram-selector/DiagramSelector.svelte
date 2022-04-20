<script>

    import {createEventDispatcher} from "svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let diagrams = [];

    const dispatch = createEventDispatcher();


    function selectDiagram(diagram) {
        dispatch("select", diagram)
    }

</script>

<h4>Select a diagram</h4>
{#if !_.isEmpty(diagrams)}

    <p>
        <Icon name="info-circle"/>
        Select a diagram from the list below to see a dynamic overlay of waltz data and any instances of this diagram
        which have been created.
    </p>

    <ul>
        {#each diagrams as diagram}
            <li>
                <button class="btn btn-skinny"
                        on:click={() => selectDiagram(diagram)}>
                    {diagram.name}
                </button>
                <span class="text-muted">
                    <LastEdited entity={diagram}/>
                </span>
            </li>
        {/each}
    </ul>

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

