<script>

    import {diagramService} from "../entity-diagram-store";
    import Icon from "../../../../common/svelte/Icon.svelte";

    const {selectedGroup} = diagramService;


</script>


{#if $selectedGroup}
    <h4>{$selectedGroup.title}</h4>
    <table class="table table-condensed table-striped small">
        <colgroup>
            <col width="50%"/>
            <col width="50%"/>
        </colgroup>
        <tbody>
        <tr>
            <td>ID</td>
            <td>{$selectedGroup.id}</td>
        </tr>
        <tr>
            <td>Alignment</td>
            <td>{$selectedGroup.props.flexDirection}</td>
        </tr>
        <tr>
            <td>Bucket Size</td>
            <td>{$selectedGroup.props.bucketSize}</td>
        </tr>
        <tr>
            <td>Proportion</td>
            <td>{$selectedGroup.props.proportion}</td>
        </tr>
        <tr>
            <td>Item Height</td>
            <td>{$selectedGroup.props.minHeight}em</td>
        </tr>
        <tr>
            <td>Item Width</td>
            <td>{$selectedGroup.props.minWidth}em</td>
        </tr>
        <tr>
            <td>Title Font Size</td>
            <td>{$selectedGroup.props.titleFontSize}em</td>
        </tr>
        <tr>
            <td>Content Font Size</td>
            <td>{$selectedGroup.props.contentFontSize}em</td>
        </tr>
        </tbody>
    </table>

{:else}
    <div class="help-block">
        <Icon name="info-circle"/> Please select an item from the tree or diagram to view more detail
    </div>
{/if}