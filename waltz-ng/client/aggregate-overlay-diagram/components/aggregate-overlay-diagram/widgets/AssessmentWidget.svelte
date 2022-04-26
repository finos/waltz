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
        .range([0,100])

    $: xMax = _.get(
        _.maxBy(counts, c => c.count),
        ["count"],
        0);

    $:x = scaleLinear()
        .domain([0, xMax])
        .range([0, 100]);

    $: rowHeight = y.bandwidth() > 40
        ? 40
        : y.bandwidth();

</script>

<h1>Hello Mum</h1>

<svg class="content" viewBox="0 0 300 100">
    {#each counts as r, idx}
        <rect x="0"
              y={y(r.rating.id)}
              width={x(r.count)}
              height={rowHeight}
              fill={r.rating.color}>
        </rect>
        <text dx="110"
              dy={y(r.rating.id) + rowHeight - 4}
              font-size={rowHeight - 1}
              fill="#666">
            {r.rating.name || "?"}
        </text>
    {/each}
</svg>
