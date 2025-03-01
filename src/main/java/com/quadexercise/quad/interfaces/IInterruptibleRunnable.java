package com.quadexercise.quad.interfaces;


@FunctionalInterface
public interface IInterruptibleRunnable {
    void run() throws InterruptedException;
}
