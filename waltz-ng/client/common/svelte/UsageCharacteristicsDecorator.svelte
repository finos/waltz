<script>

    import Icon from "./Icon.svelte";
    import _ from "lodash";

    export let usageCharacteristics;
    export let isConcrete;
    export let isEditMode = false;


    $: isEditable = usageCharacteristics?.isRemovable;
    $: isReadonly = ! isEditable;
    $: hasViewerMessage = !_.isEmpty(usageCharacteristics.warningMessageForViewers);
    $: hasEditorMessage = !_.isEmpty(usageCharacteristics.warningMessageForEditors);
    $: hasMapping= !_.isEmpty(usageCharacteristics);

</script>

{#if !isConcrete && hasMapping}
    <span class="waltz-error-icon" title="This data type is non-concrete so should not be mapped to">
        <Icon name="exclamation-triangle"
              style="vertical-align: middle"/>
    </span>
{/if}
{#if isEditMode && hasEditorMessage}
    <span class="waltz-warning-icon" title={usageCharacteristics.warningMessageForEditors}>
        <Icon name="exclamation-triangle"
              style="vertical-align: middle"/>
    </span>
{/if}
{#if !isEditMode && hasViewerMessage}
    <span class="waltz-warning-icon" title={usageCharacteristics.warningMessageForViewers}>
        <Icon name="exclamation-triangle"
              style="vertical-align: middle"/>
    </span>
{/if}
{#if !isEditMode && isReadonly && hasViewerMessage}
    <span title="read only" style="color: #777;">
        <Icon name="lock"
              style="vertical-align: middle"/>
    </span>
{/if}
{#if isEditMode && isReadonly}
    <span title="read only" style="color: #777;">
        <Icon name="lock"
              style="vertical-align: middle"/>
    </span>
{/if}