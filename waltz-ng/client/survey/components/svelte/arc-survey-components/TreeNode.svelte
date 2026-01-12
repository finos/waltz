<!--
  ~ Waltz - Enterprise Architecture
  ~ Copyright (C) 2016 - 2026 Waltz open source project
  ~ See README.md for more information
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific
  ~
  -->

<script>
    import Tooltip from "../../../../common/svelte/Tooltip.svelte";

    export let node;
    export let onSelectItem;
    export let onDeselectItem;
    export let tooltipContent = undefined;
    export let isSelected = (n) => false;
    export let mode;
    export let url;

    const MODES = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    let expanded = true;

    function toggle(event) {
        event.stopPropagation();
        expanded = !expanded;
    }

    function handleCheck(event) {
        const isChecked = event.target.checked;

        // children is the children array, rest is the node itself
        const {children, ...rest} = node;
        if (isChecked) {
            onSelectItem(rest);
        } else {
            onDeselectItem(rest);
        }
    }
</script>

<div class="tree-node">
    <Tooltip content={tooltipContent} props={{node, url}}>
        <div class="node-label" slot="target">
            {#if node.children && node.children.length > 0}
                <button class="toggle-btn" on:click={toggle}>
                    {expanded ? '▼' : '▶'}
                </button>
            {:else}
                <span class="spacer"></span>
            {/if}
            {#if mode === MODES.EDIT}
                <input type="checkbox" checked={isSelected(node)} on:change={handleCheck} class="clickable"/>
            {/if}
            <span class:name-bold={mode === MODES.VIEW && isSelected(node)} class="name">{node.title}</span>
        </div>
    </Tooltip>

    {#if expanded && node.children}
        <div class="children">
            {#each node.children as child}
                <svelte:self
                        node={child}
                        {onSelectItem}
                        {onDeselectItem}
                        {tooltipContent}
                        {isSelected}
                        {mode}
                        {url}/>
            {/each}
        </div>
    {/if}
</div>

<style>
    .tree-node {
        margin: 2px 0;
    }
    .node-label {
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 4px;
        display: flex;
        align-items: center;
        transition: background-color 0.2s;
        gap: 0.5rem;
    }
    .node-label:hover {
        background-color: #f0f0f0;
    }
    .toggle-btn {
        border: none;
        background: none;
        cursor: pointer;
        width: 20px;
        text-align: center;
        margin-right: 5px;
        font-size: 0.8em;
        color: #666;
    }
    .spacer {
        display: inline-block;
        width: 20px;
        margin-right: 5px;
    }
    .name {
        flex-grow: 1;
    }
    .name-bold {
        flex-grow: 1;
        font-weight: bold;
    }
    .children {
        padding-left: 25px;
        border-left: 1px solid #eee;
        margin-left: 12px;
    }
</style>
