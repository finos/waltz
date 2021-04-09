<script>
    import _ from "lodash";
    import Icon from "./Icon.svelte";
    import {createEventDispatcher} from "svelte";


    export let childNodes = [];
    export let node;
    export let expanded = false;
    export let isRoot = false;

    let selectedDataType;

    $: sortedNodes = _.orderBy(childNodes, d => d.name);

    const dispatcher = createEventDispatcher();

    function toggleExpanded() {
        expanded = !expanded;
    }

    function selectNode(selectedNode) {
        console.log("selecting node")
        dispatcher("select", selectedNode);
    }

</script>


{#if !isRoot}
    <span class="clickable"
          on:click={toggleExpanded}>
        <Icon size="lg" name={expanded ? "caret-down" : "caret-right"}/>
    </span>
    <span class="clickable"
          class:concrete={node.concrete}
          class:abstract={!node.concrete}
          class:unknown={node.unknown}
          class:deprecated={node.deprecated}
          on:click={() => selectNode(node)}>
        {node.name}
    </span>
{/if}

{#if expanded || node.isExpanded}
    <ul>
        {#each sortedNodes as childNode}
            <li>
                {#if childNode.children.length > 0}
                    <svelte:self on:select node={childNode} childNodes={childNode.children}/>
                {:else}
                    <span class="clickable"
                          class:concrete={childNode.concrete}
                          class:abstract={!childNode.concrete}
                          class:unknown={childNode.unknown}
                          class:deprecated={childNode.deprecated}
                          on:click={() => selectNode(childNode)}>
                        <Icon size="lg" name="fw"/>
                        {childNode.name}
                    </span>
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

    li {
        padding: 0.2em 0;
    }
</style>