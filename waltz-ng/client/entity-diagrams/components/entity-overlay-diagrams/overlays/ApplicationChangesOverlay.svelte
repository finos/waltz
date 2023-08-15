<script>

    import {scaleBand, scaleLinear, scalePow, scaleSqrt, scaleThreshold} from "d3-scale";
    import {axisBottom, axisLeft, axisRight} from "d3-axis";
    import _ from "lodash";
    import {appChangeAdditionalYears} from "./overlay-store";
    import {select} from "d3-selection";
    import {
        RenderModes
    } from "../../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/aggregate-overlay-diagram-utils";


    export let cellData = [];
    export let maxOutboundCount = 0;
    export let maxInboundCount = 0;

    export let renderMode;
    export let height;
    export let width;

    let svgElem;

    const appCountProportion = 0.1
    const axisProportion = renderMode === RenderModes.FOCUSED ? 0.05 : 0;
    const graphProportion = 1 - axisProportion - appCountProportion;

    const minYear = new Date().getFullYear();
    $: futureYear = getFutureYear($appChangeAdditionalYears)
    $: years = getYears(minYear, futureYear);

    function getFutureYear(additionalYears) {
        const currentYear = new Date().getFullYear();
        const futureYear = currentYear + additionalYears;
        console.log({currentYear, additionalYears, futureYear});
        return futureYear;
    }

    function getYears(currentYear, endYear) {
        let yearArray = [];
        let currentDate = currentYear;
        while (currentDate <= endYear) {
            yearArray.push(Number(currentDate));
            currentDate = currentDate + 1;
        }
        return yearArray;
    }

    $: maxYear = futureYear;

    $: console.log({minYear, maxYear, futureYear, years});

    $: quarters = 4 + ($appChangeAdditionalYears * 4);

    $: barWidth = width / (quarters);

    $: inboundColorScale = scaleLinear()
        .domain([minYear, futureYear -1])
        .range(["#009d37", "#e2fc6f"]);

    $: outboundColorScale = scaleLinear()
        .domain([minYear, futureYear -1])
        .range(["#b74c11", "#fce265"]);

    $: countScale = scaleSqrt()
        .domain([countScaleMin, countScaleMax])
        .range([height, 0])

    $: yearScale = scaleBand()
        .domain(years)
        .range([0, width * graphProportion]);

    $: yAxis = axisLeft(countScale)
        .tickSizeInner(0)
        .ticks([countScaleMax, countScaleMin, 0])
        .tickSizeOuter(height * 0.01)
        .tickPadding(height * 0.01);

    $: columnScale = scaleLinear()
        .domain([1, quarters])
        .range([0, width * graphProportion])

    $: {
        const svg = select(svgElem);

        svg.selectAll("text")
            .style("font-size", height * 0.1)
    }

    $: cellMaxOutbound = _.maxBy(cellData.outboundCounts, d => d.count);
    $: cellMaxInbound = _.maxBy(cellData.inboundCounts, d => d.count);

    $: countScaleMin = _.max([maxOutboundCount, _.get(cellMaxOutbound, "count", 0)]) * -1;
    $: countScaleMax = _.max([maxInboundCount,  _.get(cellMaxInbound, "count", 0)]);

    // $: console.log({maxOutbound: cellMaxOutbound?.count, maxInbound: cellMaxInbound?.count, maxInboundCount, maxOutboundCount, countScaleMin, countScaleMax});

    function mkTitle(quarter, count) {
        return `${quarter.quarterName} ${quarter.year}: ${count} applications`
    }

</script>

<div class="content">

    <div>
        <svg viewBox={`0 0 ${width} ${height}`}
             bind:this={svgElem}
             style="background: white">
            <g>
                <text transform={`translate(${width * appCountProportion / 2} ${height / 2})`}
                      text-anchor="middle"
                      dominant-baseline="middle">
                    {cellData.currentAppCount || 0}
                </text>
            </g>
            <g transform={`translate(${width * appCountProportion} 0)`}>
                {#if cellData.outboundCounts}
                    <g class="outbound">
                        {#each cellData.outboundCounts as outboundCount}
                            <rect x={columnScale(outboundCount.quarter.additionalQuarters)}
                                  y={countScale(0)}
                                  width={width / quarters}
                                  height={countScale(outboundCount.count * -1) - countScale(0)}
                                  fill={outboundColorScale(outboundCount.quarter.year)}>
                                <title>{mkTitle(outboundCount.quarter, outboundCount.count)}</title>
                            </rect>
                        {/each}
                    </g>
                {/if}
                {#if cellData.inboundCounts}
                    <g class="inbound">
                        {#each cellData.inboundCounts as inboundCount}
                            <rect x={columnScale(inboundCount.quarter.additionalQuarters)}
                                  y={countScale(inboundCount.count)}
                                  width={width / quarters}
                                  height={countScale(0) - countScale(inboundCount.count)}
                                  fill={inboundColorScale(inboundCount.quarter.year)}>
                                <title>{mkTitle(inboundCount.quarter, inboundCount.count)}</title>
                            </rect>
                        {/each}
                    </g>
                {/if}
                <g class="midline">
                    <line x1={0}
                          y1={countScale(0)}
                          x2={width}
                          y2={countScale(0)}
                          stroke="white"
                          stroke-width={height * 0.01}
                          opacity="0.5"/>
                </g>
            </g>
            <g>
                {#if renderMode === RenderModes.FOCUSED}
                    <g transform={`translate(${width * appCountProportion} 0)`}>
                        {#each years as year}
                            <text transform={`translate(${yearScale(year) + yearScale.bandwidth() / 2}, ${height})`}
                                  text-anchor="middle"
                                  dominant-baseline="auto">
                                {year}
                            </text>
                        {/each}
                    </g>
                    <g>
                        <text transform={`translate(${width}, ${countScale(countScaleMax)})`}
                              text-anchor="end"
                              dominant-baseline="hanging">
                            {countScaleMax}
                        </text>
                        <text transform={`translate(${width}, ${countScale(countScaleMin)})`}
                              text-anchor="end"
                              dominant-baseline="auto">
                            {countScaleMin}
                        </text>
                        <text transform={`translate(${width}, ${countScale(0)})`}
                              text-anchor="end"
                              dominant-baseline="middle">
                            {0}
                        </text>
                    </g>
                {/if}
            </g>
        </svg>
    </div>
</div>