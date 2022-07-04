<script>
    export let options = [];
    export let name;
    export let mandatory = false;
    export let value;
</script>

<label for={`${name}Kind`}>
    {name}
    <small class="text-muted">(required)</small>
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