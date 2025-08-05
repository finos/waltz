<script>
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import Popover from "../../../../../svelte-stores/popover-store";
    import EnumHelpPanel from "./EnumHelpPanel.svelte";

    export let options = [];
    export let name;
    export let mandatory = false;
    export let value;

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
    <small class="text-muted">(required)</small>
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