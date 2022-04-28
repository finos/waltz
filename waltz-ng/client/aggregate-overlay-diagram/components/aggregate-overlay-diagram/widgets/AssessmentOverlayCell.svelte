<script>
    import _ from "lodash";
    import {scaleBand, scaleLinear} from "d3-scale";

    export let cellData = [];
    export let maxCount = 0;

    let counts = [];
    let y;

    $: counts = _.orderBy(cellData?.counts, c => c.rating.name);

    $: y = scaleBand()
        .domain(counts.map(c => c.rating.id))
        .range([0, 90])


    $:x = scaleLinear()
        .domain([0, maxCount])
        .range([0, 90]);

    $: rowHeight = y.bandwidth() > 30
        ? 30
        : y.bandwidth();

</script>


<svg class="content" viewBox="0 0 300 100">
    <g transform="translate(10, 5)">
        {#each counts as r}
            <rect x="0"
                  y={y(r.rating.id)}
                  width={x(r.count)}
                  height={rowHeight}
                  stroke="#888"
                  fill={r.rating.color}>
            </rect>
            <text dx="100"
                  dy={y(r.rating.id) + rowHeight - 4}
                  font-size={rowHeight - 1}
                  fill="#666">
                {r.rating.name || "?"}
            </text>
        {/each}
    </g>
</svg>
