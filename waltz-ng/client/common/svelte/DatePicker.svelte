
<script>
    import Flatpickr from 'svelte-flatpickr';
    import 'flatpickr/dist/themes/airbnb.css';
    import Icon from "./Icon.svelte";
    import {createEventDispatcher} from "svelte";
    import {timeFormat} from "d3-time-format";

    export let origDate = new Date();
    export let options = {};
    export let canEdit = false;

    const format = timeFormat("%Y-%m-%d");
    const dispatch = createEventDispatcher();


    function handleSubmit(value) {
        if (originalDate.getTime() !== value.getTime()) {
            dispatch("change", value);
        }
    }
    const defaultOptions = {
        dateFormat: "Y-m-d",
        enableTime: false,
        allowInput: true,
        onChange: (d) => {
            handleSubmit(d[0]);
            mode=Modes.VIEW;
        }
    };

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    };


    let mode = Modes.VIEW;
    let originalDate = new Date(origDate);
    let value = new Date(originalDate);


    function onCancel() {
        value = new Date(originalDate);
        mode = Modes.VIEW;
    }

    function onEdit() {
        mode = Modes.EDIT;
    }

    $: mergedOptions = Object.assign({}, defaultOptions, options);
    $: formattedValue = format(value);

</script>

<main>
    {#if mode === Modes.VIEW}
        {formattedValue}
        {#if canEdit}
            <button class="btn-link"
                    on:click={onEdit}>
                <Icon name="edit"/>
                Edit
            </button>
        {/if}
    {:else if mode === Modes.EDIT}
        <Flatpickr options={mergedOptions}
                   bind:value/>
        <button class="btn btn-xs btn-default"
                on:click={onCancel}>
            <Icon name="times"/>
        </button>
    {/if}
</main>
