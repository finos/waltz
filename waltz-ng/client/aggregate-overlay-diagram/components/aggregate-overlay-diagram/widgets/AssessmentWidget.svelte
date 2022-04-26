<script>
    import _ from "lodash";
    import {scaleLinear, scaleBand} from "d3-scale";

    export let cellData;

    let counts = [];
    let xMax = 0;
    let y;

    $: counts = _.orderBy(cellData?.counts, c => c.rating.name);

    $: y = scaleBand()
        .domain(counts.map(c => c.rating.id))
        .range([0,90])

    $: xMax = _.get(
        _.maxBy(counts, c => c.count),
        ["count"],
        0);

    $:x = scaleLinear()
        .domain([0, xMax])
        .range([0, 100]);

    $: rowHeight = y.bandwidth() > 30
        ? 30
        : y.bandwidth();

</script>

<svg class="content" viewBox="0 0 300 100">
    <g transform="translate(10, 5)">
        {#each counts as r, idx}
            <rect x="0"
                  y={y(r.rating.id)}
                  width={x(r.count)}
                  height={rowHeight}
                  stroke="#888"
                  fill={r.rating.color}>
            </rect>
            <text dx="110"
                  dy={y(r.rating.id) + rowHeight - 4}
                  font-size={rowHeight - 1}
                  fill="#666">
                {r.rating.name || "?"}
            </text>
        {/each}
    </g>
</svg>
