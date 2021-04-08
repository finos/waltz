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
    <span class:expandable={node.children}
          on:click={() => {
                toggleExpanded();
                selectNode(node);
          }}>
        <Icon size="lg" name={expanded ? "caret-down" : "caret-right"}/>
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
                    <span on:click={() => selectNode(childNode)}>
                        <Icon size="lg" name="fw"/>
                        {childNode.name}
                    </span>
                {/if}
            </li>
        {/each}
    </ul>
{/if}

<style>
    .expandable{
        xxcolor: red;
}
    ul {
        list-style: none;
    }
</style>