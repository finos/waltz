<script>
    import Popover from "../../../svelte-stores/popover-store";
    import PopoverContent from "./PopoverContent.svelte";

    let height = 100;

</script>

{#if $Popover}

    <div class="waltz-backing-screen"
         on:click={() => $Popover ? Popover.dismiss() : null}
         on:keydown={() => $Popover ? Popover.dismiss() : null}>
    </div>

    <section class="waltz-svelte-popover">
        <PopoverContent on:dismiss={() => Popover.dismiss()}>
            <div slot="content">
                {#if $Popover.title}
                    <h4>{$Popover.title}</h4>
                {/if}
                <div class:waltz-scroll-region-400={height > 400}>
                    <div bind:clientHeight={height}>
                        <svelte:component this={$Popover.component} {...$Popover.props}/>
                        {#if $Popover.content}
                            <div>
                                {@html $Popover.content}
                            </div>
                        {/if}
                    </div>
                </div>
            </div>
        </PopoverContent>
    </section>
{/if}

<style>
    .waltz-backing-screen {
        position: fixed;
        width: 100%;
        height: 100%;
        background-color: #aaa;
        opacity: 0.3;
        z-index: 899;
        transition: opacity ease 0.3s;
    }

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
