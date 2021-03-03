<script>
    import {useCommonYScale} from "./stores/decorators";
    import {dynamicDate} from "./stores/selected-dates";
    import {scaleLinear} from "d3-scale";
    import {max} from "d3-array";
    import {axisBottom, axisLeft} from "d3-axis";
    import _ from "lodash";
    import {mouse, select} from "d3-selection";
    import {stack} from "d3-shape";


    const ANIMATION_DURATION = 400;
    const stacker = stack()
        .keys(['r', 'a', 'g'])
        .value((d, k) => d.values[k].length)


    export let data;
    export let dateScale;
    export let color;
    export let height = 100;
    export let width = 100;
    export let commonYScale;
    export let measurablesById;

    let el;

    $: subChartName = measurablesById[data?.k]?.name || "-";
    $: {
        const {k, stackData} = data;
        const series = stacker(stackData);


        const svg = select(el);

        svg.select("rect.background")
            .on("click.select", d => {
                const mousePosition = mouse(svg.node())[0];
                const selectedDate = x.invert(mousePosition);
                dynamicDate.set(selectedDate);
            });


        const y  = $useCommonYScale
            ? commonYScale
                .range([height, 0])
            : scaleLinear()
                .domain([0, max(series, d => max(d, d => d[1])) + 2]).nice()
                .range([height, 0]);

        const x = dateScale
            .range([0, width]);

        const xAxis = g => g
            .attr("transform", `translate(0 ${height})`)
            .call(axisBottom(x).ticks(width / 80).tickSize(3))
            .call(g => g.select(".domain").remove());

        const yAxis = g => g
            .call(axisLeft(y).ticks(height / 20).tickSize(3))
            .call(g => g.select(".domain").remove());

        const ratingBands = svg
            .select(".graph")
            .selectAll("g.rating-band")
            .data(series);

        const newRatingBands = ratingBands
            .enter()
            .append("g")
            .classed("rating-band", true);

        const ratingBandBars = ratingBands
            .merge(newRatingBands)
            .selectAll("rect")
            .data(d => d);

        const newRatingBandBars = ratingBandBars
            .enter()
            .append("rect")
            .attr("stroke", "none")
            .attr("stroke-width", 0.2)
            .attr("x", d => x(d.data.s))
            .attr("y", d => y(d[1]))
            .attr("height", d => y(d[0]) - y(d[1]))
            .attr("width", d => x(d.data.e || x.domain()[1]) - x(d.data.s))

        ratingBandBars
            .merge(newRatingBandBars)
            .classed("fade-out", d => _.isUndefined(d.data.e))
            .transition()
            .duration(ANIMATION_DURATION)
            .attr("x", d => x(d.data.s))
            .attr("y", d => y(d[1]))
            .attr("height", d => y(d[0]) - y(d[1]))
            .attr("width", d => x(d.data.e || x.domain()[1]) - x(d.data.s))
            .attr("fill", function (d)  {
                const p = select(this.parentNode).datum();
                return _.isUndefined(d.data.e)
                    ? `url(#gradient-bg-${p.key})`
                    : color.bg(p.key);
            })
            .attr("stroke", function (d)  {
                const p = select(this.parentNode).datum();
                return _.isUndefined(d.data.e)
                    ? `url(#gradient-fg-${p.key})`
                    : color.fg(p.key);
            });

        svg.select("g.x-axis")
            .call(xAxis);

        svg.select("g.y-axis")
            .call(yAxis);
    }
</script>

<g class="subChart"
   style="pointer-events: all"
   bind:this={el}>


    <g class="graph"></g>
    <g class="x-axis"></g>
    <g class="y-axis"></g>

    <text dy="5" dx="20" class="subchart-title">
        {subChartName}
    </text>

    <rect class="background" {width} {height} style="fill: none"></rect>
</g>

<style>
    .subchart-title {
        font-size: 12px;
        fill: #556666;
    }
</style>