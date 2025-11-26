<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import * as d3 from "d3";
    import { onMount, onDestroy } from "svelte";

    export let startDate;
    export let endDate;
    export let period = "month"; // This chart DOES use period!
    export let key = 0; // For forcing re-render

    let svgContainer;
    let containerWidth;
    let chartData = null;
    let loading = true;
    let error = null;
    let lastLoadKey = 0; // Track last loaded key

    // Load data when key changes
    $: if (key && startDate && endDate) {
        loadData();
    }

    onMount(() => {
        // Initial load will be handled by reactive statement
    });

    onDestroy(() => {
        if (svgContainer) {
            try {
                d3.select(svgContainer).selectAll("*").remove();
            } catch (e) {
                // Ignore cleanup errors
            }
        }
    });

    async function loadData() {
        if (!startDate || !endDate) {
            loading = false;
            return;
        }

        // Simple duplicate prevention
        const currentKey = key;
        if (currentKey === lastLoadKey) {
            return;
        }
        lastLoadKey = currentKey;

        loading = true;
        error = null;
        chartData = null;
        console.log('OperationTrendsChart loading data with key:', key, 'period:', period);
        try {
            const url = `/api/change-log-summaries/analytics/operation-trends?startDate=${startDate}&endDate=${endDate}&period=${period}`;
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            // Check if data is empty or has no valid entries
            if (!data || Object.keys(data).length === 0) {
                chartData = { title: "Operation Trends Over Time", operations: [], periods: [] };
                loading = false;
                return;
            }

            // Transform the nested data structure for line chart
            // data is: { "operation1": { "2024-01": 10, "2024-02": 15 }, "operation2": { ... } }
            const operations = Object.keys(data);
            const allPeriods = new Set();

            // Collect all periods
            operations.forEach(op => {
                Object.keys(data[op]).forEach(period => allPeriods.add(period));
            });

            const sortedPeriods = Array.from(allPeriods).sort();

            // Transform to chart format
            const operationsData = operations.map(op => ({
                name: op,
                displayName: formatOperationName(op),
                values: sortedPeriods.map(period => ({
                    period: period,
                    value: data[op][period] || 0
                }))
            }));

            // Check if all operations have zero values
            const hasNonZeroData = operationsData.some(op =>
                op.values.some(v => v.value > 0)
            );

            chartData = {
                title: "Operation Trends Over Time",
                periods: sortedPeriods,
                operations: hasNonZeroData ? operationsData : []
            };

            loading = false;
        } catch (err) {
            console.error('Operation Trends API failed:', err);
            error = 'Failed to load operation trends data';
            loading = false;
        }
    }

    function formatOperationName(operation) {
        if (!operation) return 'Unknown';
        return operation.split('_')
                       .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
                       .join(' ');
    }

    $: if (svgContainer && chartData && chartData.operations && chartData.operations.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        if (!svgContainer || !chartData || !chartData.operations) {
            return;
        }

        // Clear previous chart safely
        try {
            const container = d3.select(svgContainer);
            if (!container.empty()) {
                container.selectAll("svg").remove();
            }
        } catch (e) {
            console.warn('Error clearing chart:', e);
        }

        const margin = {top: 40, right: 150, bottom: 60, left: 60};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        // Scales
        const xScale = d3.scalePoint()
            .domain(chartData.periods)
            .range([0, width])
            .padding(0.1);

        const maxValue = d3.max(chartData.operations, d =>
            d3.max(d.values, v => v.value)
        );

        const yScale = d3.scaleLinear()
            .domain([0, maxValue])
            .nice()
            .range([height, 0]);

        // Color scale
        const colorScale = d3.scaleOrdinal(d3.schemeCategory10);

        // Add X axis
        svg.append("g")
            .attr("transform", `translate(0, ${height})`)
            .call(d3.axisBottom(xScale))
            .selectAll("text")
            .style("text-anchor", "end")
            .attr("dx", "-.8em")
            .attr("dy", ".15em")
            .attr("transform", "rotate(-45)");

        // Add Y axis
        svg.append("g")
            .call(d3.axisLeft(yScale));

        // Add grid lines
        svg.append("g")
            .attr("class", "grid")
            .attr("transform", `translate(0, ${height})`)
            .call(d3.axisBottom(xScale)
                .tickSize(-height)
                .tickFormat("")
            )
            .style("stroke-dasharray", "3,3")
            .style("opacity", 0.3);

        svg.append("g")
            .attr("class", "grid")
            .call(d3.axisLeft(yScale)
                .tickSize(-width)
                .tickFormat("")
            )
            .style("stroke-dasharray", "3,3")
            .style("opacity", 0.3);

        // Line generator
        const line = d3.line()
            .x(d => xScale(d.period))
            .y(d => yScale(d.value))
            .curve(d3.curveMonotoneX);

        // Add lines for each operation
        chartData.operations.forEach((operation, i) => {
            const color = colorScale(i);

            // Add line
            svg.append("path")
                .datum(operation.values)
                .attr("fill", "none")
                .attr("stroke", color)
                .attr("stroke-width", 2)
                .attr("d", line);

            // Add dots
            svg.selectAll(`.dot-${i}`)
                .data(operation.values)
                .enter().append("circle")
                .attr("class", `dot-${i}`)
                .attr("cx", d => xScale(d.period))
                .attr("cy", d => yScale(d.value))
                .attr("r", 4)
                .attr("fill", color)
                .style("cursor", "pointer")
                .on("mouseover", function(event, d) {
                    d3.select(this).attr("r", 6);

                    const tooltip = d3.select("body").append("div")
                        .attr("class", "tooltip")
                        .style("position", "absolute")
                        .style("background", "rgba(0,0,0,0.9)")
                        .style("color", "white")
                        .style("padding", "10px")
                        .style("border-radius", "6px")
                        .style("font-size", "13px")
                        .style("pointer-events", "none")
                        .style("z-index", "1000")
                        .style("box-shadow", "0 4px 8px rgba(0,0,0,0.3)")
                        .html(`<strong>${operation.displayName}</strong><br/>Period: ${d.period}<br/>Count: ${d.value.toLocaleString()}`)
                        .style("left", (event.pageX + 10) + "px")
                        .style("top", (event.pageY - 10) + "px");
                })
                .on("mouseout", function() {
                    d3.select(this).attr("r", 4);
                    d3.selectAll(".tooltip").remove();
                });
        });

        // Add legend
        const legend = svg.append("g")
            .attr("class", "legend")
            .attr("transform", `translate(${width + 20}, 20)`);

        chartData.operations.forEach((operation, i) => {
            const legendRow = legend.append("g")
                .attr("transform", `translate(0, ${i * 20})`);

            legendRow.append("rect")
                .attr("width", 12)
                .attr("height", 12)
                .attr("fill", colorScale(i));

            legendRow.append("text")
                .attr("x", 20)
                .attr("y", 9)
                .style("font-size", "12px")
                .style("fill", "#333")
                .text(operation.displayName);
        });

        // Add title
        svg.append("text")
            .attr("x", width / 2)
            .attr("y", -10)
            .attr("text-anchor", "middle")
            .style("font-size", "16px")
            .style("font-weight", "bold")
            .style("fill", "#333")
            .text(`Operation Trends (${period.charAt(0).toUpperCase() + period.slice(1)}ly)`);

        // Add Y axis label
        svg.append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 0 - margin.left)
            .attr("x", 0 - (height / 2))
            .attr("dy", "1em")
            .style("text-anchor", "middle")
            .style("font-size", "12px")
            .style("fill", "#666")
            .text("Number of Changes");
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Operation Trends Over Time"}
    </div>
    <div slot="content">
        <div bind:clientWidth={containerWidth} style="position: relative; min-height: 400px;">
            {#if loading}
                <LoadingPlaceholder/>
            {:else if error}
                <div class="alert alert-danger">
                    <i class="fa fa-warning"></i>
                    {error}
                </div>
            {:else if !chartData || !chartData.operations || chartData.operations.length === 0}
                <div class="alert alert-info text-center">
                    <i class="fa fa-info-circle"></i>
                    No data found
                </div>
            {:else}
                <div bind:this={svgContainer}></div>
            {/if}
        </div>
    </div>
</SubSection>

<style>
    :global(.tooltip) {
        opacity: 0.9;
    }
</style>
