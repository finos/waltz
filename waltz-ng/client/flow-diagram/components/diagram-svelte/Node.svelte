<script>
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

    $: shape = node && shapes[node.data.kind]();
    $: transform = node && `translate(${positions[node.id].x} ${positions[node.id].y})`;
</script>


<g {transform}>
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
    <text dx={shape.title.dx + 14}
          dy={shape.title.dy}
          style="font-size: small;">
        {node.data.name}
    </text>
</g>