<script>

    import DescriptionFade from "./DescriptionFade.svelte";
    import Icon from "./Icon.svelte";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import LastEdited from "./LastEdited.svelte";
    import DataTypeDecoratorViewTable
        from "../../data-types/components/data-type-decorator-section/context-panel/DataTypeDecoratorViewTable.svelte";

    export let dataType;
    export let decorator;

</script>

{#if (dataType)}
    <strong>{dataType.name}</strong>
    <div class="help-block">{dataType.code}</div>
    <DescriptionFade text={dataType.description}/>
    {#if dataType.deprecated}
        <div style="padding-top: 1em">
            <span style="color: darkred">
                <Icon name="exclamation-triangle"/>
            </span>
            This data type is deprecated
        </div>
    {/if}
{/if}
{#if decorator}
    <br>
    <DataTypeDecoratorViewTable {decorator}/>
{/if}