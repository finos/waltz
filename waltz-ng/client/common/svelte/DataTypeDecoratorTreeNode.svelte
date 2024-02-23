<script>
    import _ from "lodash";
    import Icon from "./Icon.svelte";
    import {createEventDispatcher} from "svelte";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import DataTypeDecoratorNodeContent from "./DataTypeDecoratorNodeContent.svelte";

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

    function calcChecked(filterFn, n) {
        const isUnchecked = filterFn(n);

        return !isUnchecked;
    }

    function calcDisabled(filterFn, n) {
        const isUnchecked = filterFn(n);

        return isUnchecked // should be allowed to deselect non-concrete
            && (!nonConcreteSelectable && !n.concrete)
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
    <DataTypeDecoratorNodeContent {node}
                                  {multiSelect}
                                  isDisabled={calcDisabled(selectionFilter, node)}
                                  isChecked={calcChecked(selectionFilter, node)}
                                  {selectNode}/>
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
                                 {expanded}
                                 childNodes={childNode.children}/>
                {:else}
                    <Icon size="lg"
                          name="fw"/>
                    <DataTypeDecoratorNodeContent node={childNode}
                                                  {multiSelect}
                                                  isDisabled={calcDisabled(selectionFilter, childNode)}
                                                  isChecked={calcChecked(selectionFilter, childNode)}
                                                  {selectNode}/>
                {/if}
            </li>
        {/each}
    </ul>
{/if}

<style>
    ul {
        padding: 0.2em 0 0 0.5em;
        margin: 0 0 0 0.5em;
        list-style: none;
        border-left: 1px solid #eee;
    }
</style>