<script>
    import {select} from "d3-selection";
    import {mkDragHandler} from "./drag-handler";
    import {createEventDispatcher} from "svelte";
    import _ from "lodash";
    import overlay from "./store/overlay";
    import {determineStylingBasedUponLifecycle, symbolsByName} from "./flow-diagram-utils";
    import {widths} from "./store/layout";
    import {selectedNode} from "./diagram-model-store";
    import {toGraphId} from "../../flow-diagram-utils";

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
                dy: 14
            }
        };
    }

    function mkRectShape(widthHint) {
        return {
            path: `M0,0 L${widthHint},0 L${widthHint},30 L0,30 z`,
            cx: widthHint / 2,
            cy: 10,
            title: {
                dx: 4,
                dy: 14
            }
        };
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

    let nameElem;
    let gElem;

    function getWidth(id, associatedGroups) {
        $widths[id] = determineWidth(node, nameElem, associatedGroups);
        return $widths[id];
    }

    $: width = nameElem && getWidth(node.id, associatedGroups);
    $: shape = node && shapes[node.data.kind](width);
    $: transform = node && `translate(${positions[node.id].x} ${positions[node.id].y})`;
    $: dragHandler = mkDragHandler(node);
    $: select(gElem).call(dragHandler);

    $: associatedGroups = _.filter(groups, g => _.includes(g.data.applicationIds, node.data.id));

    $: classes = [
        "wfd-node",
        $overlay.appliedOverlay && !_.includes(associatedGroups, $overlay.appliedOverlay)
            ? "wfd-not-active"
            : "wfd-active",
        $selectedNode && toGraphId($selectedNode) === node.id
            ? 'wfd-selected-node'
            : ''
    ].join(" ");

    $: nodeStyling = determineStylingBasedUponLifecycle(node.data.entityLifecycleStatus);

    function determineWidth(node, elem, icons) {
        const margin = node.data.kind === 'ACTOR'
            ? 30
            : 24;
        const textWidth = elem.getComputedTextLength() + margin;
        const iconsWidth = _.size(icons) * 12 + 6;
        return _.max([textWidth, iconsWidth]);
    }

</script>


<g {transform}
   bind:this={gElem}
   on:click={selectNode}
   on:keydown={selectNode}
   class={classes}>
    <path d={shape.path}
          fill="#fafafa"
          class="shape"
          stroke={nodeStyling.color}
          stroke-dasharray={nodeStyling.dashArray}
          style="padding-top: 20px">
    </path>
    <text style="font-size: 12px;"
          class="icon"
          font-family="FontAwesome"
          dx={shape.title.dx}
          dy={shape.title.dy}>
        {shape.icon}
    </text>
    <text dx={shape.title.dx + 16}
          dy={shape.title.dy}
          style="font-size: 14px;"
          class="name"
          bind:this={nameElem}> <!-- think this is confused, unlike d3 not id tracked? -->
        {node.data.name || "Unknown"}
    </text>
    <g transform="translate(10, 16)"
       class="wfd-node-classifiers">
        {#each associatedGroups as group}
            <path class="symbol"
                  d="{symbolsByName[group.data.symbol]()}"
                  transform="translate({0 + _.findIndex(associatedGroups, group) * 12}, {group.data.symbol === 'triangle' ? 7 :6 })"
                  fill={group.data?.fill}
                  stroke={group.data?.stroke}>
            </path>
        {/each}
    </g>
</g>


<style type="text/scss">
    @import "style/variables";

    .wfd-node {
      user-select: none;
      opacity: 0.9;
      pointer-events: all;

      &:hover {
        cursor: move;
      }
    }

    .wfd-active {
        fill: black;
    }

    .wfd-not-active {
        fill: lightgray;
        .shape {
            stroke: #eee;
        }
        .symbol {
            opacity: 0.3;
        }
    }

    .wfd-selected-node .shape {
        stroke-width: 2;
        stroke: #bea64c;
        fill: #f1eee1;
    }

</style>
