<script>
    import _ from "lodash";
    import {scaleBand, scaleSqrt} from "d3-scale";
    import {
        RenderModes
    } from "../../../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/aggregate-overlay-diagram-utils";

    export let cellData = [];
    export let maxCount = 0;
    export let maxRatings = 0;
    export let renderMode;
    export let width;
    export let height;

    let counts = [];
    let y;

    $: counts = _.orderBy(cellData?.counts, c => c.rating.name);

    $: y = scaleBand()
        .domain(counts.map(c => c.rating.id))
        .range([0, height])

    // different scale for focused view so that a consistent height is set and text is legible
    $: focusY = scaleBand()
        .domain(counts.map(c => c.rating.id))
        .range([0, height / 5 * _.size(counts)]);

    // Used in a small area so sqrt makes small values easier to see
    $: x = scaleSqrt()
        .domain([0, maxCount])
        .range([0, renderMode === RenderModes.FOCUSED ? width * 0.8 : width]);

</script>

<div>
    {#if renderMode !== RenderModes.FOCUSED}
        <svg viewBox={`0 0 ${width} ${height}`}
             style="background: white">
            <g>
                {#each counts as r}
                    <rect x={renderMode === RenderModes.FOCUSED ? width * 0.2 : "0"}
                          y={y(r.rating.id)}
                          width={x(r.count)}
                          height={height / maxRatings * 0.8}
                          fill={r.rating.color}>
                        <title>{r.rating.name}</title>
                    </rect>
                    {#if renderMode === RenderModes.FOCUSED}
                        <text x={width * 0.2}
                              y={y(r.rating.id) + (height / maxRatings * 0.4)}
                              font-size={height / maxRatings * 0.6}
                              text-anchor="end"
                              dominant-baseline="middle"
                              fill="black">
                            {r.count}
                            <title>{r.rating.name}</title>
                        </text>
                    {/if}
                {/each}
            </g>
        </svg>
    {:else if renderMode === RenderModes.FOCUSED}
        <svg viewBox={`0 0 ${width} ${height / 5 * _.size(counts)}`}
             style="background: white">
            <g>
                {#each counts as r}
                    <rect x={renderMode === RenderModes.FOCUSED ? width * 0.2 : "0"}
                          y={focusY(r.rating.id)}
                          width={x(r.count)}
                          height={height / 5 * 0.8}
                          fill={r.rating.color}>
                        <title>{r.rating.name}</title>
                    </rect>
                    {#if renderMode === RenderModes.FOCUSED}
                        <text x={width * 0.15}
                              y={focusY(r.rating.id) + (height / 5 * 0.4)}
                              font-size={height / 5 * 0.6}
                              text-anchor="end"
                              dominant-baseline="middle"
                              fill="black">
                            {r.count}
                            <title>{r.rating.name}</title>
                        </text>
                    {/if}
                {/each}
            </g>
        </svg>
    {/if}
</div>

<style>

    svg {
        display: block;
    }

</style>