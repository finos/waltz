<script>

    import {scaleLinear, scalePow, scaleSqrt} from "d3-scale";
    import {axisBottom} from "d3-axis";
    import _ from "lodash";
    import {appChangeAdditionalYears} from "./overlay-store";
    import {select} from "d3-selection";


    export let cellData = [];
    export let maxOutboundCount = 0;
    export let maxInboundCount = 0;

     // export let renderMode;
    export let height;
    export let width;

    let axisElem;
    const minYear = new Date().getFullYear();
    $: maxYear = minYear + $appChangeAdditionalYears;

    $: quarters = $appChangeAdditionalYears * 4;


    $: barWidth = width / (quarters);

    $: inboundColorScale = scalePow()
        .exponent(-1)
        .domain([1, quarters])
        .range(["#1a8a00", "#bdffac"]);

    $: outboundColorScale = scalePow()
        .exponent(-1)
        .domain([1, quarters])
        .range(["#a85c00", "#ffbd66"]);

    global.colorScale = outboundColorScale

    const axisScale = scaleLinear()
        .domain([minYear, maxYear])
        .range([0, width]);

    const countScale = scaleSqrt()
        .domain([maxOutboundCount * -1, maxInboundCount])
        .range([height, 0])

    const axis = axisBottom(axisScale);

    $: columnScale = scaleLinear()
        .domain([1, quarters])
        .range([0, width])

    $: maxOutbound = _.maxBy(cellData.outboundCounts, d => d.count);
    $: maxInbound = _.maxBy(cellData.inboundCounts, d => d.count);

    $: console.log({maxOutbound: maxOutbound?.count, maxInbound: maxInbound?.count, maxInboundCount, maxOutboundCount});

    function mkTitle(quarter) {
        return `${quarter.quarterName} ${quarter.year}`
    }

</script>

<div class="content">

    <div>
        <svg viewBox={`0 0 ${width} ${height}`}
             style="background: white">
            <g class="outbound">
                {#each cellData.outboundCounts as outboundCount}
                    <rect x={columnScale(outboundCount.quarter.additionalQuarters)}
                          y={countScale(0)}
                          width={width / quarters}
                          height={countScale(outboundCount.count * -1) - countScale(0)}
                          fill={outboundColorScale(outboundCount.quarter.additionalQuarters)}>
                        <title>{mkTitle(outboundCount.quarter)}</title>
                    </rect>
                {/each}
            </g>
            <g class="inbound">
                {#each cellData.inboundCounts as inboundCount}
                    <rect x={columnScale(inboundCount.quarter.additionalQuarters)}
                          y={countScale(inboundCount.count)}
                          width={width / quarters}
                          height={countScale(0) - countScale(inboundCount.count)}
                          fill={inboundColorScale(inboundCount.quarter.additionalQuarters)}>
                        <title>{mkTitle(inboundCount.quarter)}</title>
                    </rect>
                {/each}
            </g>
            <g>
                <line x1="0"
                      y1={countScale(0)}
                      x2={width}
                      y2={countScale(0)}
                      stroke="white"
                      stroke-width={height * 0.01}
                      opacity="0.5"/>
            </g>
        </svg>
    </div>
</div>
