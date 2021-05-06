<script>
    import _ from "lodash";
    import Icon from "./Icon.svelte";
    import {createEventDispatcher} from "svelte";

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
        dispatcher("select", selectedNode);
    }

</script>


{#if !isRoot}
    <button class="btn btn-skinny"
            on:click={toggleExpanded}>
        <Icon size="lg"
              name={expanded ? "caret-down" : "caret-right"}/>
    </button>
    <button class="btn btn-skinny"
            class:concrete={node.concrete}
            class:abstract={!node.concrete}
            class:unknown={node.unknown}
            class:deprecated={node.deprecated}
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
                          class:abstract={!childNode.concrete}
                          class:unknown={childNode.unknown}
                          class:deprecated={childNode.deprecated}
                          on:click={() => selectNode(childNode)}>
                        {childNode.name}
                        {#if childNode.deprecated}
                            <span class="label label-warning">
                                Deprecated
                            </span>
                        {/if}
                    </button>
                {/if}
            </li>
        {/each}
    </ul>
{/if}

<style>

    .concrete {

    }

    .abstract {
        font-style: italic;
    }

    .deprecated {
        color: darkred;
    }

    .unknown {
        color: gray;
    }

    ul {
        padding: 0.2em 0 0 0.5em;
        margin: 0 0 0 0.5em;
        list-style: none;
        border-left: 1px solid #eee;
    }


</style>