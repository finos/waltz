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
    import TreeNode from "./TreeNode.svelte";
    import ArcTooltip from "./ARCTooltip.svelte";

    export let onSelectItem = (r) => console.log("Item selected: ", r);
    export let onDeselectItem = (r) => console.log("Item deselected: ", r);
    export let items = [];
    export let selectedItems = [];
    export let mode;
    export let url;

    let roots = [];

    function buildTree(items) {
        if (!items) return [];
        const itemMap = new Map();
        items.forEach(item => itemMap.set(item.externalId, { ...item, children: [] }));

        const roots = [];
        itemMap.forEach(item => {
            if (item.externalParentId && itemMap.has(item.externalParentId)) {
                const parent = itemMap.get(item.externalParentId);
                if (parent) {
                    parent.children.push(item);
                }
            } else {
                roots.push(item);
            }
        });
        return roots;
    }

    $: roots = buildTree(items);

    const isSelected = (item) => !!selectedItems.find(t => t.id === item.id);

</script>

<div class="arc-tree">
    {#each roots as root}
        <TreeNode node={root}
                  {onSelectItem}
                  tooltipContent={ArcTooltip}
                  {onDeselectItem}
                  {isSelected}
                  {mode}
                  {url}/>
    {/each}
</div>

<style>
    .arc-tree {
        font-family: sans-serif;
    }
</style>
