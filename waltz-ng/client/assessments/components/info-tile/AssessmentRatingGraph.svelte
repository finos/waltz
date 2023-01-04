<script>

    import {scaleLinear} from "d3-scale";
    import _ from "lodash";
    import {determineForegroundColor} from "../../../common/colors";
    import {truncate} from "../../../common/string-utils";

    export let dimensions;
    export let graphData = [];

    $: maxCount = _.maxBy(graphData, d => d.count);

    $: xScale = scaleLinear()
        .range([0, dimensions.svg.width - dimensions.label.width])
        .domain([0, maxCount?.count]);

</script>


{#each graphData as ratingCount, i}
    <g transform="translate({dimensions.label.width}, {(dimensions.bar.height + dimensions.padding.bar) * i})">
        <rect fill={ratingCount.rating?.color}
              stroke={ratingCount.rating?.color}
              width={xScale(ratingCount.count)}
              height={dimensions.bar.height}>
        </rect>

        <text dy={(dimensions.bar.height + dimensions.padding.bar) / 2}
              dx={dimensions.padding.bar}
              font-size={dimensions.label.height}
              fill={determineForegroundColor(ratingCount.rating?.color)}>
            {ratingCount.count}
        </text>
    </g>

    <g transform="translate({dimensions.label.width - dimensions.padding.bar} {(dimensions.bar.height + dimensions.padding.bar) * i})">
        <text dy={(dimensions.bar.height + dimensions.padding.bar) / 2}
              font-size={dimensions.label.height}
              text-anchor="end"
              fill="black">
            {truncate(ratingCount.rating?.name, 20)}
        </text>
    </g>
{/each}