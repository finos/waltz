<script>
    import {mkCurvedLine} from "../../../common/d3-utils";

    export let flow;
    export let positions;

    function mkLinePath(flow) {
        const sourcePos = positions[flow.source];
        const targetPos = positions[flow.target];

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

    function mkArrowTransform(path) {
        const arrowPt1 = path.getPointAtLength(path.getTotalLength() / 1.4);
        const arrowPt2 = path.getPointAtLength(path.getTotalLength() / 1.6);

        const dx = arrowPt1.x - arrowPt2.x;
        const dy = arrowPt1.y - arrowPt2.y;
        const theta = Math.atan2(dy, dx); // (rotate marker)

        return `translate(${arrowPt1.x}, ${arrowPt1.y}) rotate(${theta * (180 / Math.PI)})`;
    }

    let elem;

    $: linePath = mkLinePath(flow);
    $: arrowTransform = elem && mkArrowTransform(elem);
</script>

<g>
    <path d={linePath}
          bind:this={elem}
          fill="none"
          stroke="black">
    </path>

    <path d="M -8,-4 8,0 -8,4 Z"
          transform={arrowTransform}
          fill="#aaa"
          stroke="#999">
    </path>
</g>