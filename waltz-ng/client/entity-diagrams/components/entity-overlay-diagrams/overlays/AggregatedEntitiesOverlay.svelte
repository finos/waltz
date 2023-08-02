<script>

    import _ from "lodash";

    export let cellData;

</script>

<div style="align-content: center; text-align: center">
    {_.size(cellData?.aggregatedEntityReferences)} entities linked to this cell
</div>