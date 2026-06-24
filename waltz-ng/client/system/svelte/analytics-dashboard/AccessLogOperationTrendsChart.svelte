<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import * as d3 from "d3";
    import { onMount, onDestroy } from "svelte";

    export let startDate;
    export let endDate;
    export let period = "month";
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
        console.log('AccessLogOperationTrendsChart loading data with key:', key, 'period:', period);
        try {
            const url = `/api/access-log/summary/period/${period}?startDate=${startDate}&endDate=${endDate}`;
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

            // Transform the data for line chart
            // Expected format: array of {period: "2024-01", count: 100}
            let transformedData = [];

            if (Array.isArray(data)) {
                transformedData = data.map(item => ({
                    period: item.period || item.date || item.key,
                    value: item.counts || item.count || item.value || item.total || 0
                }));
            } else if (typeof data === 'object') {
                transformedData = Object.entries(data).map(([period, value]) => ({
                    period: period,
                    value: value || 0
                }));
            }

            // Sort by period
            transformedData.sort((a, b) => {
                if (a.period < b.period) return -1;
                if (a.period > b.period) return 1;
                return 0;
            });

            // Check if all values are zero
            const hasNonZeroData = transformedData.some(item => item.value > 0);

            chartData = {
                title: "Access Log Trends Over Time",
                data: hasNonZeroData ? transformedData : []
            };

            loading = false;
        } catch (err) {
            console.error('Access Log Trends API failed:', err);
            error = 'Failed to load access log trends data';
            loading = false;
        }
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        if (!svgContainer || !chartData || !chartData.data) {
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

        const margin = {top: 40, right: 80, bottom: 60, left: 60};
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
            .domain(chartData.data.map(d => d.period))
            .range([0, width])
            .padding(0.1);

        const maxValue = d3.max(chartData.data, d => d.value);

        const yScale = d3.scaleLinear()
            .domain([0, maxValue])
            .nice()
            .range([height, 0]);

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

        // Add line with gradient
        const gradient = svg.append("defs")
            .append("linearGradient")
            .attr("id", "access-log-gradient")
            .attr("gradientUnits", "userSpaceOnUse")
            .attr("x1", 0).attr("y1", yScale(0))
            .attr("x2", 0).attr("y2", yScale(maxValue));

        gradient.append("stop")
            .attr("offset", "0%")
            .attr("stop-color", "#5cb85c")
            .attr("stop-opacity", 0.8);

        gradient.append("stop")
            .attr("offset", "100%")
            .attr("stop-color", "#449d44")
            .attr("stop-opacity", 1);

        // Add area under the line
        const area = d3.area()
            .x(d => xScale(d.period))
            .y0(height)
            .y1(d => yScale(d.value))
            .curve(d3.curveMonotoneX);

        svg.append("path")
            .datum(chartData.data)
            .attr("fill", "url(#access-log-gradient)")
            .attr("opacity", 0.3)
            .attr("d", area);

        // Add line
        svg.append("path")
            .datum(chartData.data)
            .attr("fill", "none")
            .attr("stroke", "#5cb85c")
            .attr("stroke-width", 3)
            .attr("d", line);

        // Add dots
        svg.selectAll(".dot")
            .data(chartData.data)
            .enter().append("circle")
            .attr("class", "dot")
            .attr("cx", d => xScale(d.period))
            .attr("cy", d => yScale(d.value))
            .attr("r", 5)
            .attr("fill", "#5cb85c")
            .attr("stroke", "#fff")
            .attr("stroke-width", 2)
            .style("cursor", "pointer")
            .on("mouseover", function(event, d) {
                d3.select(this).attr("r", 7);

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
                    .html(`<strong>Period: ${d.period}</strong><br/>Access Count: ${d.value.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function() {
                d3.select(this).attr("r", 5);
                d3.selectAll(".tooltip").remove();
            });

        // Add title
        svg.append("text")
            .attr("x", width / 2)
            .attr("y", -10)
            .attr("text-anchor", "middle")
            .style("font-size", "16px")
            .style("font-weight", "bold")
            .style("fill", "#333")
            .text(`Access Log Trends (${period.charAt(0).toUpperCase() + period.slice(1)}ly)`);

        // Add Y axis label
        svg.append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 0 - margin.left)
            .attr("x", 0 - (height / 2))
            .attr("dy", "1em")
            .style("text-anchor", "middle")
            .style("font-size", "12px")
            .style("fill", "#666")
            .text("Number of Accesses");
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Access Log Trends Over Time"}
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
            {:else if !chartData || !chartData.data || chartData.data.length === 0}
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


