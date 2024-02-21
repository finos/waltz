<script>

    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import Icon from "./Icon.svelte";
    import Tooltip from "./Tooltip.svelte";
    import DataTypeDecoratorTreeTooltip from "./DataTypeDecoratorTreeTooltip.svelte";

    export let node;
    export let multiSelect;
    export let selectNode = (d) => console.log(d);
    export let isDisabled = false;
    export let isChecked = false;

    function mkTooltipProps(node) {
        return {
            dataType: node,
            decorator: node.decorator
        }
    }

</script>

<Tooltip content={DataTypeDecoratorTreeTooltip}
         placement="left-start"
         props={mkTooltipProps(node)}>
    <svelte:fragment slot="target">
        <button class="btn btn-plain"
                class:concrete={node.concrete}
                class:abstract={!node.concrete}
                class:unknown={node.unknown}
                class:deprecated={node.deprecated}
                disabled={isDisabled}
                on:click={() => selectNode(node)}
                title={node.description}>
            {#if multiSelect}
                <Icon name={isChecked ? "square-o" : "checked-square-o}"}/>
            {/if}
            {node.name}
        </button>
            {#if node.deprecated}
                    <span class="label label-danger">
                        Deprecated
                    </span>
            {/if}
        <span class="small">
            {#if node.decorator?.flowClassification}
                <RatingIndicatorCell showName={false} {...node.decorator?.flowClassification}/>
            {/if}
            {#if node.decorator?.isReadonly}
                    <Icon name="lock"/>
            {/if}
        </span>
    </svelte:fragment>
</Tooltip>



<style>

    .concrete {
    }

    .abstract {
        font-style: italic;
    }

    .deprecated {
        color: darkred;
    }

    .unknown {
        color: gray;
    }


</style>