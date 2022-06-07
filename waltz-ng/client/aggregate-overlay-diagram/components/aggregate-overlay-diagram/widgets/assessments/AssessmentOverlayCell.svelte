<script>
    import _ from "lodash";
    import {scaleBand, scaleLinear} from "d3-scale";

    export let cellData = [];
    export let maxCount = 0;
    export let height;
    export let width;

    const rowHeight = height / 3 < defaultHeight
        ? defaultHeight
        : height / 3;

    let counts = [];
    let y;
    let defaultHeight = 14


    $: counts = _.orderBy(cellData?.counts, c => c.rating.name);

    $: svgHeight = Math.max(counts.length * rowHeight, height);

    $: y = scaleBand()
        .domain(counts.map(c => c.rating.id))
        .range([0, svgHeight])

    $:x = scaleLinear()
        .domain([0, maxCount])
        .range([0, width / 3]);


    $: textSize = rowHeight - 1


</script>


<svg class="content"
     width="100%"
     height={svgHeight + 10}
     style="background: white">
    <g transform="translate(0, 5)">
        {#each counts as r}
            <rect x="0"
                  y={y(r.rating.id)}
                  width={x(r.count)}
                  height={rowHeight}
                  stroke="#888"
                  fill={r.rating.color}>
                <title>{r.rating.name}</title>
            </rect>
            <text dx={width / 3 + 2}
                  dy={y(r.rating.id) + rowHeight / 2 + textSize / 2}
                  font-size={textSize}
                  fill="#666">
                {r.rating.name || "?"}
                <title>{r.rating.name}</title>
            </text>
        {/each}
    </g>
</svg>
