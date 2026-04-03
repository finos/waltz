<script>
    import Icon from "../../common/svelte/Icon.svelte";
    import Popover from "../../svelte-stores/popover-store";
    import EnumHelpPanel from "./EnumHelpPanel.svelte";

    export let options = [];
    export let name;
    export let mandatory = false;
    export let value;
    export let description = "";

    function showHelp() {

        const popover = {
            title: name,
            props: {options},
            component: EnumHelpPanel
        };

        Popover.add(popover);
    }

</script>

<label for={`${name}Kind`}>
    {name}
    {#if mandatory}
        <small class="text-muted">(required)</small>
    {/if}
    <span class="text-muted small" style="font-style: italic; color: #999999; margin-left: 1em;">
        {description}
    </span>
    <button class="btn btn-skinny"
            on:click|preventDefault={() => showHelp()}>
        <Icon name="question-circle"/>
    </button>
</label>

<select id={`${name}Kind`}
        class="form-control"
        required={mandatory}
        bind:value={value}>

    {#each options as d}
        <option value={d.key}
                title={d.description}>
            {d.name}
        </option>
    {/each}
</select>
<div class="help-block">
    <slot name="help"/>
</div>