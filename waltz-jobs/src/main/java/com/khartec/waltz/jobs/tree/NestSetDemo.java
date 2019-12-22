/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.jobs.tree;

/**
 * Created by dwatkins on 27/06/2016.
 */
public class NestSetDemo {

    public static void main(String[] args) {
        TreeNode food = new TreeNode(null, "food");

        TreeNode fruit = new TreeNode(food, "fruit");
        TreeNode red = new TreeNode(fruit, "red");
        TreeNode yellow = new TreeNode(fruit, "yellow");
        new TreeNode(red, "cherry");
        new TreeNode(yellow, "banana");

        TreeNode meat = new TreeNode(food, "meat");
        new TreeNode(meat, "beef");
        new TreeNode(meat, "pork");


        System.out.println(food);



        walk(food, 1);

        System.out.println(food);
    }


    private static int walk(TreeNode node, int ptr) {
        node.leftIdx = ptr;

        for (TreeNode c : node.children) {
            ptr++;
            ptr = walk(c, ptr);
        }

        node.rightIdx = ++ptr;
        return ptr;
    }
}
