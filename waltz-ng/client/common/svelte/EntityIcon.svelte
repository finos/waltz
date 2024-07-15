<script>
    import _ from "lodash";
    import {entity} from "../services/enums/entity";

    import Icon from "./Icon.svelte";
    export let kind;
    export let showName = false;

    $: iconName = _.get(entity, [kind, "icon"], "circle-o");
    $: label = _.get(entity, [kind, "name"], "");
</script>

<style>
</style>

<Icon name={iconName}/>
{#if showName}
    <span>{label}</span>
{/if}
