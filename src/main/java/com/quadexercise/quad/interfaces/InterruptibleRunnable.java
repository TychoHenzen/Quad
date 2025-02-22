package com.quadexercise.quad.interfaces;


@FunctionalInterface
public interface InterruptibleRunnable {
    void run() throws InterruptedException;
}
