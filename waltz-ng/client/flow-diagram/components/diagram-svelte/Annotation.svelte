<script>
    import {refToString} from "../../../common/entity-utils";
    import {positionFor, shapeFor, toGraphId} from "../../flow-diagram-utils";
    import {select} from "d3-selection";
    import {wrapText} from "../../../common/d3-utils";

    export let positions;
    export let annotation;

    function calcPosition(annotation) {
        const offset = positions[annotation.id];

        const entity = positions[refToString(annotation.data.entityReference)];
        return `translate(${entity.x + offset.x + 50} ${entity.y + offset.y + 10})`;
    }


    function determineAnnotationGeometry(positions, annotation) {
        const ref = annotation.data.entityReference;
        console.log("dag", {annotation, ref});

        if (ref.kind === "LOGICAL_DATA_FLOW") {
            // const geometry = {
            //     subjectPosition: {x: 0, y: 0},
            //     subjectShape: { cx: 0, cy: 0},
            //     annotationPosition: positionFor(state, d.id)
            // };
            //
            // select(`[data-flow-id="${toGraphId(ref)}"] .${styles.FLOW_ARROW}`)
            //     .each(function() {
            //         geometry.subjectPosition = calcBucketPt(select(this));
            //     });
            //
            // return geometry;
        } else {
            const subjectPosition = positions[toGraphId(ref)];
            const subjectShape = {cx: 50, cy: 5}; //shapeFor(state, toGraphId(ref));
            const annotationPosition = positions[annotation.id];
            console.log({annotation, subjectPosition, annotationPosition});
            return { subjectPosition, subjectShape, annotationPosition };
        }
    }

    let linePath = "";
    let textElem;

    $: geom = determineAnnotationGeometry(positions, annotation);
    $: {
        const bar = 120 * (geom.annotationPosition.x > 0 ? 1 : -1);
        linePath = `
                M${geom.subjectShape.cx},${geom.subjectShape.cy}
                l${geom.annotationPosition.x},${geom.annotationPosition.y}
                l${bar},0
            `;
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

    $: console.log({geom})

</script>

<g class="wfd-annotation"
   transform={`translate(${geom.subjectPosition.x} ${geom.subjectPosition.y})`}
   fill="#888">
    <path d={linePath}
          fill="none"
          stroke="#aaa"
          stroke-dasharray="4 2">
    </path>
    <circle r="12"
            stroke-dasharray="8 4"
            cx={geom.annotationPosition.x + geom.subjectShape.cx}
            cy={geom.annotationPosition.y + geom.subjectShape.cy}
            fill="none"
            stroke="#ccc"></circle>
    <text style="font-size: smaller"
          bind:this={textElem}>
    </text>
</g>

<style type="text/scss">
    $annotation-color: #a4a2a6;

    .wfd-annotation {

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