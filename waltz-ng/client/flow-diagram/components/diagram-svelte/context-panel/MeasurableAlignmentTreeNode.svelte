<script>
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";

    export let childNodes = [];
    export let node;
    export let expanded = false;
    export let isRoot = false;

    $: sortedNodes = _.orderBy(childNodes, d => d.name);

    const dispatcher = createEventDispatcher();

    function toggleExpanded() {
        expanded = !expanded;
    }

    function selectNode(selectedNode) {
        console.log({selectedNode});
        dispatcher("select", selectedNode);
    }

    $: console.log({childNodes, node})

</script>


{#if !isRoot}
    <button class="btn btn-skinny"
            on:click={toggleExpanded}>
        <Icon size="lg"
              name={expanded ? "caret-down" : "caret-right"}/>
    </button>
    <button class="btn btn-skinny"
            class:concrete={node.concrete}
            on:click={() => selectNode(node)}>
        {node.name}
    </button>
{/if}

{#if expanded || node.isExpanded}
    <ul>
        {#each sortedNodes as childNode}
            <li>
                {#if childNode.children.length > 0}
                    <svelte:self on:select node={childNode}
                                 childNodes={childNode.children}/>
                {:else}
                    <Icon size="lg"
                          name="fw"/>
                    <button class="btn btn-skinny"
                          class:concrete={childNode.concrete}
                          on:click={() => selectNode(childNode)}>
                        {childNode.name}
                    </button>
                {/if}
            </li>
        {/each}
    </ul>
{/if}

<style>

    .concrete {

    }

    ul {
        padding: 0.2em 0 0 0.5em;
        margin: 0 0 0 0.5em;
        list-style: none;
        border-left: 1px solid #eee;
    }


</style>