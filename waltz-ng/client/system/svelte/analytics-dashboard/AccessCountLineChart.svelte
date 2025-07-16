<script>
    import * as d3 from "d3";
    export let chartData = [];

    let svgContainer;
    let containerWidth;

    $: if (svgContainer && chartData.length > 0 && containerWidth) {
        d3.select(svgContainer).select("svg").remove();

        const margin = {top: 20, right: 30, bottom: 200, left: 60}; // changed bottom to 200
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom; // same as bar chart

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
            .selectAll("text")
            .attr("transform", "translate(-10,10)rotate(-60)")
            .style("text-anchor", "end");

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
            .attr("stroke", "#69b3a2")
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
            .attr("fill", "#69b3a2")
            .append("title")
            .text(d => `${d.name}\n${d.value}`);
    }
</script>

<div bind:this={svgContainer} bind:clientWidth={containerWidth}></div>