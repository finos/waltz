<script>
    import _ from "lodash";
    import {scaleBand, scaleLinear} from "d3-scale";
    import {RenderModes} from "../../aggregate-overlay-diagram-utils";
    import RatingIndicatorCell
        from "../../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";

    export let cellData = [];
    export let maxCount = 0;
    export let renderMode;

    const rowHeight = 14;

    let counts = [];
    let y;

    $: counts = _.orderBy(cellData?.counts, c => c.rating.name);

    $: height = Math.max(counts.length * rowHeight, rowHeight);

    $: y = scaleBand()
        .domain(counts.map(c => c.rating.id))
        .range([0, height])

    $:x = scaleLinear()
        .domain([0, maxCount])
        .range([0, 30]);

</script>

<div>
    <svg width="100%"
         height={height + 10}
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
                <text dx="32"
                      dy={y(r.rating.id) + rowHeight - 4}
                      font-size={rowHeight - 1}
                      fill="#666">
                    {r.rating.name || "?"}
                    <title>{r.rating.name}</title>
                </text>
            {/each}
        </g>
    </svg>

    {#if renderMode === RenderModes.FOCUSED}
        <table class="small table table-condensed">
            {#each counts as row}
                <tr>
                    <td>
                        <RatingIndicatorCell {...row.rating}/>
                        ({row.count})
                    </td>
                </tr>
            {/each}
        </table>
    {/if}
</div>

