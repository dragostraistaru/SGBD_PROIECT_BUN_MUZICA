package org.example.sgbd_proiect_bun_muzica.util;

public class RunBenchmark {
    public static void main(String[] args) {
        ConnectionPoolBenchmark.runBenchmark();
        ConnectionLeakDemo.runDemo();
    }
}