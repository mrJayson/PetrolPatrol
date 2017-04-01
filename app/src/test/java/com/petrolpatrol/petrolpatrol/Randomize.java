package com.petrolpatrol.petrolpatrol;

import java.util.Random;

public class Randomize {

    private final Random random;
    private final long seed;

    public Randomize() {
        random = new Random();
        seed = random.nextLong();
    }

    public long getSeed() {
        return seed;
    }

    public String nextString(int length) {
        StringBuilder builder = new StringBuilder();
        String candidateLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            builder.append(candidateLetters.charAt(random.nextInt(candidateLetters.length())));
        }
        return builder.toString();
    }

    public int nextInt(int upperBound) {
        return random.nextInt(upperBound);
    }

    public double nextDouble(int upperBound) {
        return random.nextInt(upperBound) + random.nextDouble();
    }
}
