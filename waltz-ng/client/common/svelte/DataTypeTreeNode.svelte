<script>
    import _ from "lodash";
    import Icon from "./Icon.svelte";
    import {createEventDispatcher} from "svelte";

    export let selectionFilter = () => true;
    export let multiSelect = false;
    export let nonConcreteSelectable = true;
    export let childNodes = [];
    export let node;
    export let expanded = false;
    export let isRoot = false;

    $: sortedNodes = _.orderBy(childNodes, [d => d.deprecated, d => d.name]);

    const dispatcher = createEventDispatcher();

    function toggleExpanded() {
        expanded = !expanded;
    }

    function selectNode(selectedNode) {
        dispatcher("select", selectedNode);
    }

    function calcCheckIcon(filterFn, n) {
        const isUnchecked = filterFn(n);

        return isUnchecked
            ? 'square-o'
            : 'check-square-o';
    }

</script>


{#if !isRoot}
    <button class="btn btn-plain"
            on:click={toggleExpanded}>
        <Icon size="lg"
              name={expanded
                        ? "caret-down fw"
                        : "caret-right fw"}/>
    </button>
    <button class="btn btn-plain"
            class:concrete={node.concrete}
            class:abstract={!node.concrete}
            class:unknown={node.unknown}
            class:deprecated={node.deprecated}
            disabled={!nonConcreteSelectable && !node.concrete}
            on:click={() => selectNode(node)}>
            {#if multiSelect}
                <Icon name={calcCheckIcon(selectionFilter, node)}/>
            {/if}
            {node.name}
    </button>
{/if}

{#if expanded || node.isExpanded}
    <ul>
        {#each sortedNodes as childNode}
            <li>
                {#if childNode.children.length > 0}
                    <svelte:self on:select
                                 {multiSelect}
                                 node={childNode}
                                 {selectionFilter}
                                 {nonConcreteSelectable}
                                 childNodes={childNode.children}/>
                {:else}
                    <Icon size="lg"
                          name="fw"/>
                    <button class="btn btn-plain"
                            class:concrete={childNode.concrete}
                            class:abstract={!childNode.concrete}
                            class:unknown={childNode.unknown}
                            class:deprecated={childNode.deprecated}
                            disabled={!nonConcreteSelectable && !childNode.concrete}
                            on:click={() => selectNode(childNode)}>
                        <span class="no-wrap">
                            {#if multiSelect}
                                <Icon name={calcCheckIcon(selectionFilter, childNode)}/>
                            {/if}
                            {childNode.name}
                            {#if childNode.deprecated}
                                <span style="background-color: red">
                                    Deprecated
                                </span>
                            {/if}
                        </span>

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