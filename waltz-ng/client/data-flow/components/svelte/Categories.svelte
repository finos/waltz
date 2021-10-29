<script>
    import {categoryScale, filteredCategories, highlightClass} from "./flow-decorator-store";
    import {dimensions} from "./flow-decorator-utils"
    import {truncateMiddle} from "../../../common/string-utils";
    import {symbol, symbolCross, symbolCircle} from "d3-shape";
    import {createEventDispatcher} from "svelte";
    import {layoutDirection, layoutDirections} from "./flow-decorator-store";


    let dispatch = createEventDispatcher();

    function onMouseEnter(category) {
        $highlightClass = `category_${category.id}`;
    }

    function onMouseLeave() {
        $highlightClass = null;
    }

    function drillDownCategory(category){
        dispatch("select", category)
    }

</script>

{#each $filteredCategories as category}
    <g transform={`translate(0, ${$categoryScale(category.id)})`}
       class="no-text-select">
        <rect fill="#f4fff0"
              stroke="#ccc"
              on:mouseenter={() => onMouseEnter(category)}
              on:mouseleave={() => onMouseLeave()}
              rx={dimensions.category.height / 2}
              width={dimensions.category.width}
              height={$categoryScale.bandwidth()}/>
        <text dx="16"
              transform={`translate(${$layoutDirection === layoutDirections.categoryToClient ? 0 : 20} )`}
              dy={$categoryScale.bandwidth() / 2 + 5}
              pointer-events="none">
             {truncateMiddle(category.name, 22)}
        </text>
        {#if category.hasChildren}
            <g transform={`translate(${$layoutDirection === layoutDirections.categoryToClient ? dimensions.category.width - 20 : 20 }, ${$categoryScale.bandwidth() / 2})`}>
                <path d={symbol().type(symbolCircle).size(500)()}
                      on:click|stopPropagation={() => drillDownCategory(category)}
                      class="clickable drilldown"
                      fill="#f4fff0"
                      style="stroke: #bbb; stroke-dasharray: 2,2"
                      stroke-opacity="0">
                </path>
                <path d={symbol().type(symbolCross).size(40)()}
                      pointer-events="none"
                      fill="#AFDE96"
                      stroke="#aaa">
                </path>
            </g>
        {/if}
    </g>
{/each}


<style>

    .drilldown:hover {
        stroke-opacity: 1;
        fill: #f9fff7;
        transition: stroke-opacity 300ms linear;
    }

</style>