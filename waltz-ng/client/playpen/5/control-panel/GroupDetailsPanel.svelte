<script>

import {selectedGroup} from "../diagram-builder-store";
import Icon from "../../../common/svelte/Icon.svelte";

</script>



{#if $selectedGroup}
    <h4>{$selectedGroup.title}</h4>

    <table class="table table-condensed table-striped">
        <colgroup>
            <col width="20%"/>
            <col width="80%"/>
        </colgroup>
        <tr>
            <td>ID</td>
            <td>{$selectedGroup.id}</td>
        </tr>
        <tr>
            <td>Item Kind</td>
            <td>{$selectedGroup.itemKind || "Not Specified"}</td>
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
    </table>

{:else}
    <div class="help-block">
        <Icon name="info-circle"/> Please select an item from the tree or diagram to view more detail
    </div>
{/if}