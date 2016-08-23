package com.khartec.waltz.jobs;

import java.util.Random;

/**
 * Created by dwatkins on 20/08/2016.
 */
public class Gaussian {

    public static void main(String[] args) {
        Random r = new Random();

        long mean = 100;
        double z = mean / 3.4;

        for (int i = 0; i < 100; i++) {
            System.out.println(r.nextGaussian() * z + mean);
        }
    }
}
