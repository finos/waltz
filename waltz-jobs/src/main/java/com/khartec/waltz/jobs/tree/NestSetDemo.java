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
