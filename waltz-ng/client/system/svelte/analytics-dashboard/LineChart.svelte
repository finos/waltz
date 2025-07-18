<script>
    import * as d3 from "d3";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte"
    export let chartData = [];
    export let lineColor = "#46baff";
    export let bulletColor = "#235D80";

    let svgContainer;
    let containerWidth;

    $: if (svgContainer && chartData.length > 0 && containerWidth) {
        d3.select(svgContainer).select("svg").remove();

        const margin = {top: 20, right: 30, bottom: 20, left: 60};
        const width = containerWidth - margin.left - margin.right;
        const height = 200 - margin.top - margin.bottom;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        // X scale (ordinal)
        const x = d3.scalePoint()
            .domain(chartData.map(d => d.name))
            .range([0, width]);

        svg.append("g")
            .attr("transform", `translate(0, ${height})`)
            .call(d3.axisBottom(x))

        // Y scale (linear)
        const y = d3.scaleLinear()
            .domain([0, d3.max(chartData, d => d.value)])
            .range([height, 0]);

        svg.append("g")
            .call(d3.axisLeft(y));

        // Line generator
        const line = d3.line()
            .x(d => x(d.name))
            .y(d => y(d.value));

        svg.append("path")
            .datum(chartData)
            .attr("fill", "none")
            .attr("stroke", lineColor)
            .attr("stroke-width", 2)
            .attr("d", line);

        // Points
        svg.selectAll("circle")
            .data(chartData)
            .enter()
            .append("circle")
            .attr("cx", d => x(d.name))
            .attr("cy", d => y(d.value))
            .attr("r", 4)
            .attr("fill", bulletColor)
            .append("title")
            .text(d => `${d.name}: ${d.value.toLocaleString()}`);
    }
</script>
<div bind:clientWidth={containerWidth} style="position: relative;">
    {#if chartData.length === 0}
        <LoadingPlaceholder/>
    {:else}
        <div bind:this={svgContainer}></div>
    {/if}
</div>