<script>
    import {refToString} from "../../../common/entity-utils";

    export let positions = {};
    export let annotations = [];

    function calcPosition(annotation) {
        const offset = positions[annotation.id];

        const entity = positions[refToString(annotation.data.entityReference)];
        return `translate(${entity.x + offset.x + 50} ${entity.y + offset.y + 10})`;
    }
</script>

{#each annotations as annotation}
    <g transform={calcPosition(annotation)}
       fill="#888">
        <text>{annotation.data.note}</text>
    </g>
{/each}