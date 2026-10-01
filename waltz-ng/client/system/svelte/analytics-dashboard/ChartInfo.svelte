<script>
    import Tooltip from "../../../common/svelte/Tooltip.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import ChartHelpContent from "./ChartHelpContent.svelte";

    export let help;
</script>

<Tooltip content={ChartHelpContent}
         props={{text: help}}
         placement="top">
    <span slot="target" class="chart-info">
        <Icon name="info-circle"/>
    </span>
</Tooltip>

<style>
    .chart-info {
        color: #959595;
        cursor: help;
        font-size: 20px;
        line-height: 1;
        vertical-align: middle;
    }
</style>
