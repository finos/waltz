<script>
    import _ from "lodash";
    import Icon from "./Icon.svelte";
    import {createEventDispatcher} from "svelte";
    import RatingCharacteristicsDecorator from "./RatingCharacteristicsDecorator.svelte";
    import UsageCharacteristicsDecorator from "./UsageCharacteristicsDecorator.svelte";
    import DataTypeNodeTooltipContent from "./DataTypeNodeTooltipContent.svelte";
    import Tooltip from "./Tooltip.svelte";

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

    function calcDisabled(filterFn, n) {
        const isUnchecked = filterFn(n);

        return isUnchecked // should be allowed to deselect non-concrete
            && (!nonConcreteSelectable && !n.concrete)
    }

    function mkTooltipProps(node) {
        return {
            ...node
        }
    }

</script>

{#if !isRoot}
    <button class="btn btn-plain"
            on:click={toggleExpanded}>
        <Icon size="xl"
              name={expanded
                        ? "caret-down fw"
                        : "caret-right fw"}/>
    </button>
    <Tooltip content={DataTypeNodeTooltipContent}
             placement="left-start"
             props={mkTooltipProps(node)}>
        <svelte:fragment slot="target">
            <button class="btn btn-plain"
                    class:concrete={node.concrete}
                    class:abstract={!node.concrete}
                    class:unknown={node.unknown}
                    class:deprecated={node.deprecated}
                    disabled={calcDisabled(selectionFilter, node)}
                    on:click={() => selectNode(node)}>
                    {#if multiSelect}
                        <span class:clickable={node.concrete}>
                            <Icon size="xl"
                                  name={calcCheckIcon(selectionFilter, node)}/>
                        </span>
                    {/if}
                    {node.name}
            </button>
            <span class="decorator-icons">
                {#if node.ratingCharacteristics}
                    <RatingCharacteristicsDecorator ratingCharacteristics={node.ratingCharacteristics}
                                                    hasDecorator={!_.isEmpty(node.usageCharacteristics)}/>
                {/if}
                    {#if node.usageCharacteristics}
                    <UsageCharacteristicsDecorator usageCharacteristics={node.usageCharacteristics}
                                                   isConcrete={node.concrete}/>
                {/if}
            </span>
        </svelte:fragment>
    </Tooltip>
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
                    <Icon size="xl"
                          name="fw"/>
                    <Tooltip content={DataTypeNodeTooltipContent}
                             placement="left-start"
                             props={mkTooltipProps(childNode)}>
                        <svelte:fragment slot="target">
                            <button class="btn btn-plain"
                                    class:concrete={childNode.concrete}
                                    class:abstract={!childNode.concrete}
                                    class:unknown={childNode.unknown}
                                    class:deprecated={childNode.deprecated}
                                    disabled={!nonConcreteSelectable && !childNode.concrete}
                                    on:click={() => selectNode(childNode)}>
                                <span class="no-wrap">
                                    {#if multiSelect}
                                        <span class:clickable={childNode.concrete}>
                                            <Icon size="xl"
                                                  name={calcCheckIcon(selectionFilter, childNode)}/>
                                        </span>
                                    {/if}
                                    {childNode.name}
                                    {#if childNode.deprecated}
                                        <span class="deprecated">
                                            (Deprecated)
                                        </span>
                                    {/if}
                                </span>
                            </button>
                            <div class="decorator-icons"
                                 style="display: inline-block">
                                {#if childNode.ratingCharacteristics}
                                    <RatingCharacteristicsDecorator ratingCharacteristics={childNode.ratingCharacteristics}
                                                                    hasDecorator={!_.isEmpty(childNode.usageCharacteristics)}/>
                                {/if}
                                {#if childNode.usageCharacteristics}
                                    <UsageCharacteristicsDecorator usageCharacteristics={childNode.usageCharacteristics}
                                                                   isConcrete={childNode.concrete}/>
                                {/if}
                            </div>
                        </svelte:fragment>
                    </Tooltip>
                {/if}
            </li>
        {/each}
    </ul>
{/if}

<style type="text/scss">
    @import "../../../style/_variables";

    .concrete {
        color: $waltz-font-color;
    }

    .abstract {
        font-style: italic;
    }

    .deprecated {
        color: red;
    }

    .unknown {
        color: gray;
    }

    .clickable {
        color: $waltz-blue;
    }

    ul {
        padding: 0.2em 0 0 0.5em;
        margin: 0 0 0 0.5em;
        list-style: none;
        border-left: 1px solid #eee;
    }


</style>