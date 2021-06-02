<script>
    import {drag} from "d3-drag";
    import {event, select} from "d3-selection";
    import {processor} from "./diagram-model-store";
    import {mkDragHandler} from "./drag-handler";

    export let node = null;
    export let positions = {};




    function mkTrapezoidShape(widthHint) {
        return {
            path: `M0,0 L${widthHint},0 L${widthHint - 5},20 L5,20 z`,
            cx: widthHint / 2,
            cy: 10,
            title: {
                dx: 8,
                dy: 13
            }
        };
    }

    function mkRectShape(widthHint) {
        const shape = {
            path: `M0,0 L${widthHint},0 L${widthHint},20 L0,20 z`,
            cx: widthHint / 2,
            cy: 10,
            title: {
                dx: 4,
                dy: 13
            }
        };
        return shape
    }

    const shapes = {
        ACTOR: (widthHint = 100) => Object.assign({}, mkTrapezoidShape(widthHint), { icon: "\uf2be"}), // user-circle-o
        APPLICATION: (widthHint = 100) => Object.assign({}, mkRectShape(widthHint), { icon: "\uf108" }),  // desktop
        EUC: (widthHint = 100) => Object.assign({}, mkRectShape(widthHint), { icon: "\uf109" }), // laptop
        DEFAULT: (widthHint = 100) => Object.assign({}, mkRectShape(widthHint), { icon: "\uf096" })
    };

    let nameElem;
    let gElem;

    $: width = nameElem && nameElem.getComputedTextLength() + 30;
    $: shape = node && shapes[node.data.kind](width);
    $: transform = node && `translate(${positions[node.id].x} ${positions[node.id].y})`;
    $: dragHandler = mkDragHandler(node, $processor)
    $: select(gElem).call(dragHandler);

</script>


<g {transform}
   bind:this={gElem}
   class="wfd-node">
    <path d={shape.path}
          class="node"
          fill="#fafafa"
          stroke="#ccc">
    </path>
    <text style="font-size: small;"
          font-family="FontAwesome"
          dx={shape.title.dx}
          dy={shape.title.dy}>
        {shape.icon}
    </text>
    <text dx={shape.title.dx + 16}
          dy={shape.title.dy}
          style="font-size: small;"
          bind:this={nameElem}>
        {node.data.name}
    </text>
</g>


<style type="text/scss">
    @import "style/variables";

    .wfd-node {
        //@extend .no-text-select;
        opacity: 0.9;
        transition: opacity 300ms;

        &,.wfd-title {
              font-size: xx-small;
              fill: $waltz-font-color;
        }

        &:hover {
             cursor: move;
            path {
                stroke: #999;
            }
        }
    }
</style>
