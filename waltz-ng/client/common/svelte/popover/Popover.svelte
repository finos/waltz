<script>
    import Popover from "../../../svelte-stores/popover-store";
    import PopoverContent from "./PopoverContent.svelte";
    import _ from "lodash";

    let height = 100;
    let elem;


    $: {
        if ($Popover) {

            _.each(elem?.children, elem => elem.parentNode.removeChild(elem));

            if ($Popover.component) {
                new $Popover.component({
                    target: elem,
                    props: $Popover.props
                });
            }
        }
    }

</script>

{#if $Popover}
    <section class="waltz-svelte-popover">
        <PopoverContent on:dismiss={() => Popover.dismiss()}>
            <div slot="content">
                <div style="width: 100%; height: 100%">
                    {#if $Popover.title}
                        <h4>{$Popover.title}</h4>
                    {/if}
                    <div class:waltz-scroll-region-400={height > 400}>
                        <div bind:clientHeight={height}>
                            <div bind:this={elem}></div>
                            {#if $Popover.content}
                                <div>
                                    {@html $Popover.content}
                                </div>
                            {/if}
                        </div>
                    </div>
                </div>
            </div>
        </PopoverContent>
    </section>
{/if}

<style>
    .waltz-svelte-popover {
        position: fixed;
        top: 20%;
        right: 20%;
        left: 20%;
        z-index: 900;
        background-color: rgb(255, 255, 255);
        border: 1px solid #999999;
        box-shadow: 2px 2px 2px 2px rgba(153, 153, 153, 0.58);
    }
</style>
