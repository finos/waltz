<script>

    import Icon from "./Icon.svelte";
    import _ from "lodash";

    export let usageCharacteristics;
    export let isConcrete;
    export let isEditMode = false;

</script>


{#if !isConcrete && !_.isEmpty(usageCharacteristics)}
    <span class="waltz-error-icon" title="This data type is non-concrete so should not be mapped to">
        <Icon name="exclamation-triangle"
              style="vertical-align: middle"/>
    </span>
{/if}
{#if isEditMode && !_.isEmpty(usageCharacteristics.warningMessageForEditors)}
    <span class="waltz-warning-icon" title={usageCharacteristics.warningMessageForEditors}>
        <Icon name="exclamation-triangle"
              style="vertical-align: middle"/>
    </span>
{/if}
{#if !isEditMode && !_.isEmpty(usageCharacteristics.warningMessageForViewers)}
    <span class="waltz-warning-icon" title={usageCharacteristics.warningMessageForViewers}>
        <Icon name="exclamation-triangle"
              style="vertical-align: middle"/>
    </span>
{/if}