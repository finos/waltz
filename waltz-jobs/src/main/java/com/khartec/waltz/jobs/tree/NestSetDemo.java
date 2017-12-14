/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
