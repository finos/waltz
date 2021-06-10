<script>
    import {select} from "d3-selection";
    import {mkCurvedLine} from "../../../common/d3-utils";
    import {createEventDispatcher} from "svelte";
    import {refToString} from "../../../common/entity-utils";
    import {positions} from "./store/layout";


    const dispatch = createEventDispatcher();

    export let flow;
    export let sourcePos;
    export let targetPos;
    export let decorations;

    function determineIcon(count) {
        if (count === 0) {
            return "\uf29c"; // question
        } else if (count === 1) {
            return "\uf016"; // one
        } else {
            return "\uf0c5"; // many
        }
    }

    function determineFlowStyling(status) {
        switch (status) {
            case "PENDING":
                return {color: "#629fcd", dashArray: "6 3"};
            case "REMOVED":
                return {color: "#888", dashArray: "3 6"};
            default:
                return {color: "black", dashArray: "0"};
        }
    }

    function mkLinePath(f, start, end) {
        const sourceShape = {
            cx: 30,
            cy: 15
        };

        const targetShape = {
            cx: 30,
            cy: 15
        };

        return mkCurvedLine(
            start.x + sourceShape.cx,
            start.y + sourceShape.cy,
            end.x + targetShape.cx,
            end.y + targetShape.cy);
    }


    function mkArrowTransform(elem) {
        const arrowPt1 = elem.getPointAtLength(elem.getTotalLength() / 1.4);
        const arrowPt2 = elem.getPointAtLength(elem.getTotalLength() / 1.6);

        const dx = arrowPt1.x - arrowPt2.x;
        const dy = arrowPt1.y - arrowPt2.y;
        const theta = Math.atan2(dy, dx); // (rotate marker)

        return `translate(${arrowPt1.x}, ${arrowPt1.y}) rotate(${theta * (180 / Math.PI)})`;
    }


    function calcBucketPosition(elem) {
        return elem.getPointAtLength(elem.getTotalLength() / 2);
    }

    function selectBucket() {
        dispatch("selectFlow", { flow: flow.data, decorations: _.map(decorations[flow.id], d=> d.data)});
    }

    let gElem;

    $: g = gElem && select(gElem);
    $: line = g && g.select(".wfd-flow-arrow");
    $: arrow = g && g.select(".wfd-flow-arrow-head");
    $: bucket = g && g.select(".wfd-flow-bucket");
    $: decorationCount = _.size(decorations[flow.id]) || 0;
    $: icon = determineIcon(decorationCount);
    $: linePath = mkLinePath(flow, sourcePos, targetPos);
    $: flowStyling = determineFlowStyling(flow.data.entityLifecycleStatus);

    $: {
        if (line && arrow) {
            // splitting these as separate reactive statements causes issues
            // looks like the reactivity may be rate limited which results in the
            // arrow heads sometimes being 'left-behind' if nodes are moved quickly
            // line.attr("d", mkLinePath(flow, sourcePos, targetPos))
            line.attr("d", linePath);
            arrow.attr("transform", mkArrowTransform(line.node()));
            const bucketPosition = calcBucketPosition(line.node());
            bucket
                .attr("transform", `translate(${bucketPosition.x}, ${bucketPosition.y})`)
            positions.setPosition({id: flow.id, x: bucketPosition.x, y: bucketPosition.y});
        }
    }
</script>

<g class="wfd-flow wfd-flow-lifecycle-{flow.data.entityLifecycleStatus}"
   data-flow-id={refToString(flow.data)}
   bind:this={gElem}>
    <path fill="none"
          class="wfd-flow-arrow"
          stroke={flowStyling.color}
          stroke-dasharray={flowStyling.dashArray}>
    </path>

    <path d="M -8,-4 8,0 -8,4 Z"
          fill="#aaa"
          class="wfd-flow-arrow-head"
          stroke={flowStyling.color}
          stroke-dasharray={flowStyling.dashArray}>
    </path>

    <g class="wfd-flow-bucket"
       on:click={selectBucket}>
        <circle r={decorationCount > 0 ? 16 : 12}
                stroke={flowStyling.color}
                fill="#fff"
                stroke-dasharray={flowStyling.dashArray}>
        </circle>
        <text style="font-size: small;"
              font-family="FontAwesome"
              text-anchor="middle"
              dx="0"
              dy="4.5"
              fill={flowStyling.color}>
            {icon}
        </text>
    </g>
</g>

<style>
    .wfd-flow-bucket {
        user-select: none;
        pointer-events: all;
    }

</style>