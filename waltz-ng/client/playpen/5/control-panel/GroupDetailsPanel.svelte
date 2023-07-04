<script>

import {selectedGroup} from "../diagram-builder-store";
import Icon from "../../../common/svelte/Icon.svelte";

</script>



{#if $selectedGroup}
    <h4>{$selectedGroup.title}</h4>
    <ul>
        <li>ID: {$selectedGroup.id}</li>
        <li>Item Kind: {$selectedGroup.itemKind}</li>
        <li>Properties:</li>
        <li>
            <ul>
                <li>Item Height: {$selectedGroup.props.itemHeight}em</li>
                <li>Item Width: {$selectedGroup.props.itemWidth}em</li>
                <li>Alignment: {$selectedGroup.props.flexDirection}</li>
                <li>Bucket Size: {$selectedGroup.props.bucketSize}</li>
                <li>Proportion: {$selectedGroup.props.proportion}</li>
            </ul>
        </li>
    </ul>
{:else}
    <div class="help-block">
        <Icon name="info-circle"/> Please select an item from the tree or diagram to view more detail
    </div>
{/if}