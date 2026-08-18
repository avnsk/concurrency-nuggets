package com.concurrency;

import java.util.concurrent.Callable;

public class App {
  public static void main(String[] args) {
    System.out.println("=== Starting Concurrency Tests ===\n");

    // ---------------------------------------------------------
    // Test 1: Successful Task Execution
    // ---------------------------------------------------------
    try {
      Callable<String> task =
          () -> {
            System.out.println("Worker thread: Task started, simulating heavy work (1 second)...");
            Thread.sleep(1000);
            return "Hello from Worker Thread via MyFuture!";
          };

      MyFuture<String> myFuture = new MyFuture<>(task);

      // Simulate a thread pool worker picking up the task
      Thread worker = new Thread(myFuture, "Worker-Thread-1");
      worker.start();

      System.out.println("Main thread: Task submitted. Now calling get() (will block)...");
      String result = myFuture.get();
      System.out.println("Main thread: Success! Result -> " + result);

    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("\n-----------------------------------\n");

    // ---------------------------------------------------------
    // Test 2: Exceptional Task Execution (Error Handling)
    // ---------------------------------------------------------
    try {
      Callable<String> failingTask =
          () -> {
            System.out.println("Worker thread: Failing task started...");
            Thread.sleep(500);
            throw new IllegalStateException("Database connection failed!");
          };

      MyFuture<String> failingFuture = new MyFuture<>(failingTask);
      Thread worker2 = new Thread(failingFuture, "Worker-Thread-2");
      worker2.start();

      System.out.println("Main thread: Failing task submitted. Calling get()...");
      failingFuture.get(); // This should catch and rethrow the exception

    } catch (Exception e) {
      System.out.println("Main thread caught expected exception: " + e.getCause().getMessage());
    }

    System.out.println("\n=== Tests Completed ===");
  }
}
