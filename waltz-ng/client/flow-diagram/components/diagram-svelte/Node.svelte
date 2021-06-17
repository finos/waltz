<script>
    import {select} from "d3-selection";
    import {processor} from "./diagram-model-store";
    import {mkDragHandler} from "./drag-handler";
    import {createEventDispatcher} from "svelte";
    import _ from "lodash";
    import overlay from "./store/overlay";


    const dispatch = createEventDispatcher();

    export let node = null;
    export let positions = {};
    export let groups = {};

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
            path: `M0,0 L${widthHint},0 L${widthHint},30 L0,30 z`,
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

    function selectNode() {
        dispatch("selectNode", node.data);
    }

    function determineFill(overlayGroup, associatedGroups){
        if (_.isNil(overlayGroup)){
            return "#fafafa"
        } else if (_.includes(associatedGroups, overlayGroup)){
            return overlayGroup.data.fill;
        } else {
            return "#eee"
        }
    }

    let nameElem;
    let gElem;

    $: console.log({node, nameElem, width})
    $: width = nameElem && nameElem.getComputedTextLength() + 30;
    $: shape = node && shapes[node.data.kind](width);
    $: transform = node && `translate(${positions[node.id].x} ${positions[node.id].y})`;
    $: dragHandler = mkDragHandler(node)
    $: select(gElem).call(dragHandler);

    $: associatedGroups = _.filter(groups, g => _.includes(g.data.applicationIds, node.data.id))

    $: fill = determineFill($overlay.appliedOverlay, associatedGroups);

</script>


<g {transform}
   bind:this={gElem}
   on:click={selectNode}
   class="wfd-node">
    <path d={shape.path}
          class="node"
          fill={fill}
          class:wfd-node-overlay={_.includes(associatedGroups, $overlay.appliedOverlay)}
          class:wfd-node-fade={$overlay.appliedOverlay && !_.includes(associatedGroups, $overlay.appliedOverlay)}
          stroke="#ccc">
    </path>
    <text style="font-size: small;"
          class:wfd-node-fade={$overlay.appliedOverlay && !_.includes(associatedGroups, $overlay.appliedOverlay)}
          font-family="FontAwesome"
          dx={shape.title.dx}
          dy={shape.title.dy}>
        {shape.icon}
    </text>
    <text dx={shape.title.dx + 16}
          dy={shape.title.dy}
          style="font-size: small;"
          class:wfd-node-fade={$overlay.appliedOverlay && !_.includes(associatedGroups, $overlay.appliedOverlay)}
          bind:this={nameElem}> <!-- think this is confused, unlike d3 not id tracked? -->
        {node.data.name || "Unknown"}
    </text>
    <g transform="translate(10, 16)"
       class="wfd-node-classifiers">
        {#each associatedGroups as group}
            <circle r="4"
                    fill={group.data?.fill}
                    stroke={group.data?.stroke}
                    cx={0 + _.findIndex(associatedGroups, group) * 12}
                    cy="6"/>
        {/each}
    </g>
</g>


<style type="text/scss">
    @import "style/variables";

    .wfd-node {
        user-select: none;
        opacity: 0.9;
        transition: opacity 300ms;
        pointer-events: all;

        &:hover {
            cursor: move;
            path {
                stroke: #999;
            }
        }

        .wfd-node-overlay {
            opacity: 0.5;
        }

        .wfd-node-fade {
            opacity: 0.5;
            color: #bbb;
        }

    }
</style>
