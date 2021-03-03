<script>

    import {scaleBand, scaleOrdinal, scaleSqrt, scaleUtc} from "d3-scale";
    import TestData from "../test-data";

    import Defs from "./Defs.svelte"
    import SubChart from "./SubChart.svelte";
    import Controls from "./Controls.svelte";
    import {calcDateExtent, toStackData} from "../milestone-utils";
    import DateGuideLines from "./DateGuideLines.svelte";
    import DetailView from "./DetailView.svelte";

    import {greenBg, amber, amberBg, green, red, redBg} from "../../../common/colors";
    import {measurableStore} from "./stores/measurables";


    let measurables = measurableStore.loadAll();

    $: measurablesById = _.keyBy($measurables.data, d => d.id);

    const width = 400, height = 600;


    const margin = {
        left: 60,
        right: 20,
        top: 20,
        bottom: 20
    };


    let el;
    let svg;
    let data = TestData;
    let dateScale;
    let stacks = [];
    let commonYScale;

    $: {
        const groupedByVenue = _.groupBy(
            data,
            d => d.id_b);

        stacks = _.map(
            groupedByVenue,
            (v, k) => ({k, stackData: toStackData(v)}));
    }

    const color = {
        bg: scaleOrdinal()
            .domain(['r', 'a', 'g'])
            .range([redBg, amberBg, greenBg]),
        fg: scaleOrdinal()
            .domain(['r', 'a', 'g'])
            .range([red, amber, green])
    };

    $: y = scaleBand()
        .domain(_.map(stacks, d => d.k))
        .range([0, height - (margin.top + margin.bottom)])
        .padding(0.3);

    $: {
        const maxY = _
            .chain(stacks)
            .map(s => s.stackData)
            .flatMap(d => _.map(d, d => d.values))
            .map(d => _.sum(_.map(d, (xs, k) => _.size(xs))))
            .max()
            .value();

        commonYScale = scaleSqrt()
            .domain([0, maxY]).nice();

        dateScale = scaleUtc()
            .domain(calcDateExtent(data, 30 * 12));
    }

</script>

<div class="row">
    <div class="col-md-12">
        <h4>Controls</h4>
        <Controls/>
    </div>
</div>

<div class="row">
    <div class="col-sm-7">
        <svg bind:this={el}
             viewBox="0 0 {width} {height}">
            <Defs colors={color} />

            <g transform="translate({margin.left} {margin.top})">
                {#each stacks as subChart}
                    <g transform="translate(0 {y(subChart.k)})">
                        <SubChart data={subChart}
                                  color={color}
                                  {dateScale}
                                  width={width - (margin.left + margin.right)}
                                  height={y.bandwidth()}
                                  {commonYScale}
                                  {measurablesById}/>
                    </g>
                    <DateGuideLines {dateScale}
                                    height={height}
                                    width={width - (margin.left + margin.right)}/>
                {:else}
                    <text dy="50" dx="10">No Data</text>
                {/each}
            </g>
        </svg>
    </div>
    <div class="col-sm-5">
        <DetailView data={stacks}
                    {measurablesById}/>
    </div>
</div>

<style>
    svg {
        max-width: 560px;
    }
</style>