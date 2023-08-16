<script>

    import {scaleBand, scaleLinear, scaleOrdinal, scalePow, scaleSqrt, scaleThreshold} from "d3-scale";
    import {axisBottom, axisLeft, axisRight} from "d3-axis";
    import _ from "lodash";
    import {appChangeAdditionalYears} from "./overlay-store";
    import {select} from "d3-selection";
    import {
        RenderModes
    } from "../../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/aggregate-overlay-diagram-utils";
    import {schemeOrRd, schemeBuGn} from "d3";
    import {reverse} from "../../../../common/list-utils";


    export let cellData = [];
    export let maxOutboundCount = 0;
    export let maxInboundCount = 0;

    export let renderMode;
    export let height;
    export let width;

    let svgElem;

    const appCountProportion = renderMode === RenderModes.FOCUSED ? 0.1 : 0.25;
    const axisProportion = renderMode === RenderModes.FOCUSED ? 0.1 : 0;
    const paddingProportion = renderMode === RenderModes.FOCUSED ? 0.1 : 0;
    const graphHeightProportion = 1 - (axisProportion + appCountProportion + paddingProportion * 2);
    const graphWidthProportion = 1 - axisProportion;

    const minYear = new Date().getFullYear();
    $: futureYear = getFutureYear($appChangeAdditionalYears)

    $: maxYear = futureYear - 1; // Date picker selects up to the 1st of the next year, only need up to the end of the previous year included
    $: years = getIncrements(minYear, maxYear) || [];

    function getFutureYear(additionalYears) {
        const currentYear = new Date().getFullYear();
        return currentYear + additionalYears;
    }

    function getIncrements(current, end) {
        let incrementArray = [];
        let currentIncrement = current;
        while (currentIncrement <= end) {
            incrementArray.push(Number(currentIncrement));
            currentIncrement = currentIncrement + 1;
        }
        return incrementArray;
    }

    $: console.log({minYear, futureYear, years});

    $: quarters = _.size(years) * 4;

    $: barWidth = width / (quarters);

    $: requiredColoursCount = _.min([9, _.max([5, _.size(years)])]);

    $: outboundColors = reverse(schemeOrRd[requiredColoursCount]);
    $: inboundColors = reverse(schemeBuGn[requiredColoursCount]);

    $: inboundColorScale = scaleOrdinal()
        .domain(_.map(years, d => d.toString())) //years)
        .range(inboundColors);

    $: outboundColorScale = scaleOrdinal()
        .domain(_.map(years, d => d.toString())) //years)
        .range(outboundColors);

    $: inboundColorScale.unknown(_.last(inboundColors));
    $: outboundColorScale.unknown(_.last(outboundColors));

    $: countScale = scaleSqrt()
        .domain([countScaleMin, countScaleMax])
        .range([height * graphHeightProportion, 0])


    $: yearScale = scaleBand()
        .domain(years)
        .range([0, width * graphWidthProportion]);

    $: yAxis = axisLeft(countScale)
        .tickSizeInner(0)
        .ticks([countScaleMax, countScaleMin, 0])
        .tickSizeOuter(height * 0.01)
        .tickPadding(height * 0.01);

    $: columnScale = scaleBand()
        .domain(getIncrements(1, quarters))
        .range([0, width * graphWidthProportion]);

    $: cellMaxOutbound = !_.isEmpty(cellData) ? _.maxBy(cellData.outboundCounts, d => d.count) : 0;
    $: cellMaxInbound = !_.isEmpty(cellData) ? _.maxBy(cellData.inboundCounts, d => d.count) : 0;

    $: countScaleMin = _.max([maxOutboundCount, _.get(cellMaxOutbound, "count", 0)]) * -1;
    $: countScaleMax = _.max([maxInboundCount,  _.get(cellMaxInbound, "count", 0)]);

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
                <text transform={`translate(${width * graphWidthProportion / 2} ${height - (height * appCountProportion / 2)})`}
                      text-anchor="middle"
                      font-size={height * appCountProportion}
                      dominant-baseline="middle">
                    # Apps: {cellData?.currentAppCount || 0} (+{cellData?.totalInboundCount} / -{cellData?.totalOutboundCount})
                </text>
            </g>
            <g>
                {#if cellData.outboundCounts}
                    <g class="outbound">
                        {#each cellData.outboundCounts as outboundCount}
                            <rect x={columnScale(outboundCount.quarter.additionalQuarters)}
                                  y={countScale(0)}
                                  width={yearScale.bandwidth() / 4}
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
                                  width={yearScale.bandwidth() / 4}
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
                          x2={width * graphWidthProportion}
                          y2={countScale(0)}
                          stroke="#ccc"
                          stroke-width={height * 0.01}
                          opacity="0.8"/>
                </g>
            </g>
            <g>
                {#if renderMode === RenderModes.FOCUSED}
                    <g>
                        {#each years as year}
                            <text transform={`translate(${yearScale(year) + yearScale.bandwidth() / 2} ${height * graphHeightProportion + height * paddingProportion + (height * axisProportion / 2)})`}
                                  text-anchor="middle"
                                  font-size={height * axisProportion}
                                  dominant-baseline="middle">
                                {year}
                            </text>
                        {/each}
                    </g>
                    <g>
                        <text transform={`translate(${width}, ${countScale(countScaleMax)})`}
                              text-anchor="end"
                              font-size={height * axisProportion}
                              dominant-baseline="hanging">
                            {countScaleMax}
                        </text>
                        <text transform={`translate(${width}, ${countScale(countScaleMin)})`}
                              text-anchor="end"
                              font-size={height * axisProportion}
                              dominant-baseline="auto">
                            {countScaleMin}
                        </text>
                        <text transform={`translate(${width}, ${countScale(0)})`}
                              text-anchor="end"
                              font-size={height * axisProportion}
                              dominant-baseline="middle">
                            {0}
                        </text>
                    </g>
                {/if}
            </g>
        </svg>
    </div>
</div>