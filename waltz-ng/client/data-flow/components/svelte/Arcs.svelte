<script>
    import {
        categoryScale,
        clientScale,
        clientScrollOffset,
        filteredArcs,
        highlightClass,
        layout,
        rainbowTipProportion
    } from "./flow-decorator-store";
    import _ from "lodash";
    import {dimensions} from "./flow-decorator-utils";
    import {select, selectAll} from "d3-selection";
    import {scaleOrdinal} from "d3-scale";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";
    const {reverse} = require("../../../common/list-utils");

    function calcScreenArcs(offset, clientY, categoryY, lineColorScale, arcs) {

        const start = (offset * -1) - 10;
        const end = (offset * -1) + dimensions.diagram.height - 10;
        return _.map(
            arcs,
            a => {
                const left = $layout.left;
                const right = $layout.right;
                const calcY = (direction, a) => direction.scale(direction.id(a))
                    + direction.offset(offset)
                    + direction.scale.bandwidth() / 2
                const pos = clientY(a.clientId);
                const showing = pos > start && pos < end;
                const horizon = (pos <= start && pos > start - 100) || (pos >= end && pos < end + 100); // just outside of the scroll area

                const y1 = calcY(left, a);
                const y2 = calcY(right, a);

                const isLinear = y1 === y2;

                const defId = isLinear ? `url(#static_linear_grad_${a.id})`: `url(#static_grad_${a.id})`;

                const color = lineColorScale(a.ratingId);

                let classes = _.compact([
                    "arc",
                    showing ? "showing" : null,
                    horizon ? "horizon" : null,
                    a.lifecycleStatus === 'PENDING' ? "pending" : null,
                    `category_${a.categoryId}`,
                    `client_${a.clientId}`
                ]);
                return {
                    x1: left.dimensions.width,
                    x2: dimensions.diagram.width - right.dimensions.width,
                    y1,
                    y2,
                    stroke: showing ? defId : color,
                    classes,
                    defId,
                    color,
                    showing,
                    arc: a,
                };
            });
    }


    $: {
        if ($highlightClass) {
            selectAll(`.arc.showing`).classed("faded", true);
            selectAll(`.arc.showing.${$highlightClass}`).classed("highlight", true).classed("faded", false);
            $rainbowTipProportion = 0.35;

            selectAll(`.arc.showing.highlight`)
                .each(function() {
                    const elem = select(this);
                    const arcId = elem.attr("data-arc-id");
                    const isLinear = elem.attr("linear");
                    elem.attr("stroke", isLinear === "true" ? `url(#dynamic_linear_grad_${arcId})` : `url(#dynamic_grad_${arcId})`);
                });
        } else {
            selectAll(`.arc.showing.highlight`)
                .each(function() {
                    const elem = select(this);
                    const arcId = elem.attr("data-arc-id");
                    const isLinear = elem.attr("linear");
                    elem.attr("stroke", isLinear === "true" ? `url(#static_linear_grad_${arcId})` : `url(#static_grad_${arcId})`);
                })
            rainbowTipProportion.set(0.05, {delay: 0, duration: 0});
            selectAll(".arc").classed("highlight", false).classed("faded", false);
        }
    }

    $: screenArcs = calcScreenArcs(
        $clientScrollOffset,
        $clientScale,
        $categoryScale,
        lineColorScale,
        $filteredArcs);

    $: screenDefs = _
        .chain(screenArcs)
        .filter(d => d.showing)
        .map(d => {

            const total = _.sumBy(
                d.arc.tipRatings,
                r => r.count);

            let currentStop = 0;

            const colorStops = _
                .chain(d.arc.tipRatings)
                .filter(t => t.count > 0)
                .map(t => {
                    const colorSpaceNeeded = t.count / total;
                    return { color: lineColorScale(t.ratingId), colorSpaceNeeded, ratingId: t.ratingId}
                })
                .orderBy(d => d.ratingId)
                .map(d => {
                    const offsetStart = currentStop;
                    currentStop = currentStop + d.colorSpaceNeeded;
                    return Object.assign({}, d, { offsetStart, offsetEnd: currentStop })
                })
                .value()

            return {
                defId: d.arc.id,
                color: d.color,
                colorStops,
            };
        })
        .value();

    $: flowClassificationCall = flowClassificationStore.findAll()
    $: flowClassifications = $flowClassificationCall.data;

    $: lineColorScale = scaleOrdinal()
        .domain(_.map(flowClassifications, d => d.id))
        .range(_.map(flowClassifications, d => d.color));

</script>

<defs>
    {#each screenDefs as def}

        <!-- DYNAMIC -->
        <linearGradient id={`dynamic_grad_${def.defId}`} gradientUnits="objectBoundingBox">
            {#each reverse(def.colorStops) as colorStop}
                <stop stop-color={colorStop.color} offset={($rainbowTipProportion) - colorStop.offsetEnd * $rainbowTipProportion}/>
                <stop stop-color={colorStop.color} offset={($rainbowTipProportion) - colorStop.offsetStart * $rainbowTipProportion}/>
            {/each}
            <stop stop-color={def.color} offset={$rainbowTipProportion}/> <!-- middle of bar is solid color -->
            <stop stop-color={def.color} offset={1 - $rainbowTipProportion}/> <!-- rainbow tip - main color - rainbow tip -->
            {#each def.colorStops as colorStop}
                <stop stop-color={colorStop.color} offset={(1 - $rainbowTipProportion) + colorStop.offsetStart * $rainbowTipProportion}/>
                <stop stop-color={colorStop.color} offset={(1 - $rainbowTipProportion) + colorStop.offsetEnd * $rainbowTipProportion}/>
            {/each}
        </linearGradient>
        <linearGradient id={`dynamic_linear_grad_${def.defId}`} gradientUnits="userSpaceOnUse">
            {#each reverse(def.colorStops) as colorStop}
                <stop stop-color={colorStop.color} offset={($rainbowTipProportion + 0.08) - colorStop.offsetEnd * ($rainbowTipProportion + 0.08)}/>
                <stop stop-color={colorStop.color} offset={($rainbowTipProportion + 0.08) - colorStop.offsetStart * ($rainbowTipProportion + 0.08)}/>
            {/each}
            <stop stop-color={def.color} offset={0 + $rainbowTipProportion + 0.1}/> <!-- middle of bar is solid color -->
            <stop stop-color={def.color} offset={1 - $rainbowTipProportion - 0.1}/> <!-- rainbow tip - main color - rainbow tip -->
            {#each def.colorStops as colorStop}
                <stop stop-color={colorStop.color} offset={(1 - $rainbowTipProportion - 0.08) + colorStop.offsetStart * ($rainbowTipProportion + 0.08)}/>
                <stop stop-color={colorStop.color} offset={(1 - $rainbowTipProportion - 0.08) + colorStop.offsetEnd * ($rainbowTipProportion + 0.08)}/>
            {/each}
        </linearGradient>

        <!-- STATIC -->
        <linearGradient id={`static_linear_grad_${def.defId}`} gradientUnits="userSpaceOnUse">
            {#each reverse(def.colorStops) as colorStop}
                <stop stop-color={colorStop.color} offset={0.35 - colorStop.offsetEnd * 0.35}/>
                <stop stop-color={colorStop.color} offset={0.35 - colorStop.offsetStart * 0.35}/>
            {/each}
            <stop stop-color={def.color} offset="0.37"/>
            <stop stop-color={def.color} offset="0.63"/>
            {#each def.colorStops as colorStop}
                <stop stop-color={colorStop.color} offset={0.65 + colorStop.offsetStart * 0.35}/>
                <stop stop-color={colorStop.color} offset={0.65 + colorStop.offsetEnd * 0.35}/>
            {/each}
        </linearGradient>
        <linearGradient id={`static_grad_${def.defId}`} gradientUnits="objectBoundingBox">
            {#each reverse(def.colorStops) as colorStop}
                <stop stop-color={colorStop.color} offset={0.05 - colorStop.offsetEnd * 0.05}/>
                <stop stop-color={colorStop.color} offset={0.05 - colorStop.offsetStart * 0.05}/>
            {/each}
            <stop stop-color={def.color} offset="0.1"/>
            <stop stop-color={def.color} offset="0.9"/>
            {#each def.colorStops as colorStop}
                <stop stop-color={colorStop.color} offset={0.95 + colorStop.offsetStart * 0.05}/>
                <stop stop-color={colorStop.color} offset={0.95 + colorStop.offsetEnd * 0.05}/>
            {/each}
        </linearGradient>
    {/each}
</defs>

{#each screenArcs as arc}
    <line x1={arc.x1}
          x2={arc.x2}
          y1={arc.y1}
          y2={arc.y2}
          class={arc.classes.join(" ")}
          stroke={arc.stroke}
          data-arc-id={arc.arc.id}
          linear={arc.y1 === arc.y2}/>
{/each}

<style>
    line {
        opacity: 0.2;
        stroke-width: 1;
        transition: stroke-width 600ms, opacity 300ms;
    }

    line.showing {
        opacity: 0.8;
        stroke-width: 1.5;
    }

    line.horizon {
        opacity: 0.4;
        stroke-width: 1;
    }

    line.highlight {
        opacity: 1;
        stroke-width: 2.5;
    }

    line.faded {
        opacity: 0.3;
    }

    line.pending {
        stroke-dasharray: 8, 4;
    }
</style>