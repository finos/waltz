<script>
    import {select} from "d3-selection";
    import {mkCurvedLine} from "../../../common/d3-utils";

    export let flow;
    export let positions;

    function mkLinePath(f) {
        const sourcePos = positions[f.source];
        const targetPos = positions[f.target];

        const sourceShape = {
            cx: 50,
            cy: 10
        };

        const targetShape = {
            cx: 50,
            cy: 10
        };

        return mkCurvedLine(
            sourcePos.x + sourceShape.cx,
            sourcePos.y + sourceShape.cy,
            targetPos.x + targetShape.cx,
            targetPos.y + targetShape.cy);
    }

    function mkArrowTransform(elem) {
        const arrowPt1 = elem.getPointAtLength(elem.getTotalLength() / 1.4);
        const arrowPt2 = elem.getPointAtLength(elem.getTotalLength() / 1.6);

        const dx = arrowPt1.x - arrowPt2.x;
        const dy = arrowPt1.y - arrowPt2.y;
        const theta = Math.atan2(dy, dx); // (rotate marker)

        return `translate(${arrowPt1.x}, ${arrowPt1.y}) rotate(${theta * (180 / Math.PI)})`;
    }

    let gElem;

    $: g = gElem && select(gElem);
    $: line = g && g.select(".wfd-flow-arrow");
    $: arrow = g && g.select(".wfd-flow-arrow-head");

    $: {
        if (line && arrow) {
            // splitting these as separate reactive statements causes issues
            // looks like the reactivity may be rate limited which results in the
            // arrow heads sometimes being 'left-behind' if nodes are moved quickly
            line.attr("d", mkLinePath(flow))
            arrow.attr("transform", mkArrowTransform(line.node()));
        }
    }

</script>

<g bind:this={gElem}>
    <path fill="none"
          class="wfd-flow-arrow"
          stroke="black">
    </path>

    <path d="M -8,-4 8,0 -8,4 Z"
          fill="#aaa"
          class="wfd-flow-arrow-head"
          stroke="#999">
    </path>
</g>