<script>
    import {refToString} from "../../../common/entity-utils";
    import {select} from "d3-selection";
    import {wrapText} from "../../../common/d3-utils";
    import {draw, fade} from "svelte/transition"
    import {mkDragHandler} from "./drag-handler";
    import {createEventDispatcher} from "svelte";
    import {shapeFor} from "../../flow-diagram-utils";

    export let positions;
    export let annotation;

    const dispatch = createEventDispatcher();

    function determineAnnotationGeometry(positions, annotation) {
        const ref = annotation.data.entityReference;
        const refStr = refToString(ref);
        const annotationPosition = positions[annotation.id];
        const subjectPosition = positions[refStr] || {x: 0, y: 0};
        const subjectShape = shapeFor(ref);
        return { subjectPosition, subjectShape, annotationPosition };
    }

    let linePath = "";
    let textElem;
    let elbowElem;

    $: geom = determineAnnotationGeometry(positions, annotation);
    $: {
        const bar = 120 * (geom.annotationPosition.x > 0 ? 1 : -1);
        linePath = `
                M${geom.subjectShape.cx},${geom.subjectShape.cy}
                l${geom.annotationPosition.x},${geom.annotationPosition.y}
                l${bar},0`;
    }

    $: {
        const bar = geom.annotationPosition.x > 0
            ? 10
            : 120 * -1;

        const x = geom.annotationPosition.x + bar + geom.subjectShape.cx;
        const y = geom.annotationPosition.y + 18;

        select(textElem)
            .attr("transform", `translate(${x} ${y})`)
            .text(annotation.data.note)
            .call(wrapText, 115);
    }

    $: dragHandler = mkDragHandler(annotation)
    $: select(elbowElem).call(dragHandler);

    function selectAnnotation(){
        dispatch("selectAnnotation", annotation.data);
    }

</script>

<g class="wfd-annotation"
   transform={`translate(${geom.subjectPosition.x} ${geom.subjectPosition.y})`}
   fill="#888"
   on:click={selectAnnotation}
   on:keydown={selectAnnotation}>
    <path d={linePath}
          in:draw="{{duration: 1500}}"
          out:draw="{{duration: 2000}}"
          fill="none"
          stroke="#ddd"
          stroke-dasharray="4 2">
    </path>
    <circle r="12"
            stroke-dasharray="8 4"
            cx={geom.annotationPosition.x + geom.subjectShape.cx}
            cy={geom.annotationPosition.y + geom.subjectShape.cy}
            fill="none"
            bind:this={elbowElem}
            stroke="#ccc"></circle>
    <text style="font-size: 14px;"
          in:fade="{{ delay: 500, duration: 1000}}"
          out:fade="{{ duration: 1500}}"
          bind:this={textElem}>
    </text>
</g>


<style type="text/scss">
    $annotation-color: #a4a2a6;

    .wfd-annotation {

      user-select: none;

      &:hover circle {
            stroke-opacity: 1;
            stroke: darken($annotation-color, 20);
            cursor: move;
        }

        circle {
            fill: none;
            stroke: #ccc;
            stroke-opacity: 0;
            transition: stroke-opacity 600ms;
            stroke-dasharray: 5,4;
            pointer-events: visible;
        }

        path {
            fill: none;
            stroke: $annotation-color;
            stroke-dasharray: 2,2;
        }

        text {
            font-size: 0.6em;
            fill: darken($annotation-color, 20);
        }
    }
</style>