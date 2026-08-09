package com.example;

/** Hello world! */
public class App {
  public static void main(String[] args) throws InterruptedException {
    CustomThreadPool pool = new CustomThreadPool(10, 2);

    // Submit 5 separate tasks
    for (int i = 1; i <= 5; i++) {
      final int taskId = i;
      pool.submit(
          () -> {
            System.out.println(
                "Executing Task " + taskId + " via " + Thread.currentThread().getName());
            try {
              Thread.sleep(500);
            } catch (InterruptedException e) {
              System.out.println("Task " + taskId + " was interrupted.");
            }
          });
    }

    Thread.sleep(2000);
    System.out.println("Shutting down the thread pool...");
    pool.shutdown();
  }
}
