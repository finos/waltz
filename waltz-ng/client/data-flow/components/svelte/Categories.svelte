<script>
    import {filteredCategories, categoryScale, highlightClass} from "./scroll-store";
    import {dimensions} from "./scroll-utils"
    import {truncateMiddle} from "../../../common/string-utils";


    function onMouseEnter(category) {
        $highlightClass = `category_${category.id}`;
    }

    function onMouseLeave() {
        $highlightClass = null;
    }

</script>

{#each $filteredCategories as category}
    <g transform={`translate(0, ${$categoryScale(category.id)})`}>
        <rect fill="#f4fff0"
              stroke="#ccc"
              on:mouseenter={() => onMouseEnter(category)}
              on:mouseleave={() => onMouseLeave()}
              rx={dimensions.category.height / 2}
              width={dimensions.category.width}
              height={$categoryScale.bandwidth()}/>
        <text dx="16"
              dy={$categoryScale.bandwidth() / 2 + 8}
              pointer-events="none">
             {truncateMiddle(category.name, 24)}
        </text>
    </g>
{/each}