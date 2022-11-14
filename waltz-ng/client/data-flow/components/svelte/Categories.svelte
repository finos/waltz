<script>
    import {
        categoryScale,
        displayedCategories,
        highlightClass,
        layoutDirection,
        layoutDirections,
        parentCategory,
        startingCategory
    } from "./flow-decorator-store";
    import {colors, dimensions} from "./flow-decorator-utils"
    import {truncateMiddle} from "../../../common/string-utils";
    import {symbol, symbolCircle, symbolTriangle} from "d3-shape";
    import {createEventDispatcher} from "svelte";

    const navigationIndentation= 30;

    export let kind = 'APPLICATION';

    let dispatch = createEventDispatcher();


    function mkTranslateStr(offset) {
        return `translate(${offset.x}, ${offset.y})`;
    }


    function onMouseEnter(category) {
        $highlightClass = `category_${category.id}`;
    }

    function onMouseLeave() {
        $highlightClass = null;
    }

    function navigateToCategory(category){
        dispatch("select", category)
    }

    $: textOffset = {
        x: navigationIndentation + 18,
        y: $categoryScale.bandwidth() / 2
    };

    $: downNavigationOffset = {
        x: $layoutDirection === layoutDirections.categoryToClient
            ? dimensions.category.width - navigationIndentation
            : navigationIndentation,
        y: $categoryScale.bandwidth() / 2
    };

    $: upNavigationOffset = {
        x: $layoutDirection === layoutDirections.categoryToClient
            ? navigationIndentation
            : dimensions.category.width - navigationIndentation,
        y: $categoryScale.bandwidth() / 2
    };

</script>

<rect fill={colors[kind].fill}
      stroke={colors[kind].stroke}
      x={20}
      width={dimensions.category.width - 40}
      height={dimensions.diagram.height}>
</rect>


{#each $displayedCategories as category}
    <g transform={`translate(0, ${$categoryScale(category.id)})`}
       class="no-text-select category">
        <rect class="pill"
              on:mouseenter={() => onMouseEnter(category)}
              on:mouseleave={() => onMouseLeave()}
              rx={dimensions.category.height / 2}
              width={dimensions.category.width}
              height={$categoryScale.bandwidth()}/>
        <text dy="5"
              transform={mkTranslateStr(textOffset)}
              pointer-events="none">
             {truncateMiddle(category.name, 22)}
        </text>
        {#if category.hasChildren}
            <g transform={mkTranslateStr(downNavigationOffset)}
               class="navigation-control">
                <path d={symbol().type(symbolCircle).size(500)()}
                      on:click,keydown|stopPropagation={() => navigateToCategory(category)}
                      class="clickable navigation-circle">
                </path>
                <path d={symbol().type(symbolTriangle).size(40)()}
                      transform="rotate(180)"
                      class="navigation-arrow"
                      pointer-events="none">
                </path>
            </g>
        {/if}
        {#if $startingCategory}
            <g transform={mkTranslateStr(upNavigationOffset)}
               class="navigation-control">
                <path d={symbol().type(symbolCircle).size(500)()}
                      on:click,keydown|stopPropagation={() => navigateToCategory($parentCategory)}
                      class="clickable navigation-circle">
                </path>
                <path d={symbol().type(symbolTriangle).size(40)()}
                      class="navigation-arrow"
                      pointer-events="none">
                </path>
            </g>
        {/if}
    </g>
{/each}


<style>
    .pill {
        stroke: #ccc;
        fill: #f4fff0;
    }

    .navigation-circle {
        fill: #f4fff0;
        stroke: #bbb;
        stroke-dasharray: 2,2;
        stroke-opacity: 0.5;
    }

    .navigation-arrow {
        fill: none;
        stroke: #aaa;
    }

    .category:hover .navigation-circle {
        stroke-opacity: 1;
        fill: #f9fff7;
    }

    .category:hover .navigation-arrow {
        fill: #31eaee;
    }

    .category:hover .pill {
        fill: #e6fcdd;
    }
</style>