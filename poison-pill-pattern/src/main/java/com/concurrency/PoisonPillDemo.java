package com.concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PoisonPillDemo {

  // A distinct sentinel object acting as our Poison Pill
  private static final String POISON_PILL = "POISON_PILL_MARKER";

  static class Producer implements Runnable {
    private final BlockingQueue<String> queue;
    private final int producerId;
    private final int taskCount;

    public Producer(BlockingQueue<String> queue, int producerId, int taskCount) {
      this.queue = queue;
      this.producerId = producerId;
      this.taskCount = taskCount;
    }

    @Override
    public void run() {
      try {
        for (int i = 1; i <= taskCount; i++) {
          String task = "Task-" + producerId + "-" + i;
          queue.put(task); // Blocks if the queue is full (handles backpressure)
          System.out.println("[Producer " + producerId + "] Produced: " + task);
          Thread.sleep(100); // Simulate work generation time
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.err.println("Producer was interrupted.");
      }
    }
  }

  static class Consumer implements Runnable {
    private final BlockingQueue<String> queue;
    private final int consumerId;

    public Consumer(BlockingQueue<String> queue, int consumerId) {
      this.queue = queue;
      this.consumerId = consumerId;
    }

    @Override
    public void run() {
      try {
        while (true) {
          // Blocks until a task or poison pill becomes available
          String task = queue.take();

          // Check for the Poison Pill
          if (task.equals(POISON_PILL)) {
            System.out.println(
                "[Consumer " + consumerId + "] Received Poison Pill. Shutting down gracefully.");
            break;
          }

          // Process the task
          System.out.println("[Consumer " + consumerId + "] Processing -> " + task);
          Thread.sleep(200); // Simulate task execution time
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.err.println("[Consumer " + consumerId + "] Interrupted.");
      }
    }
  }

  public static void main(String[] args) {
    int capacity = 5;
    int numConsumers = 3;
    BlockingQueue<String> queue = new ArrayBlockingQueue<>(capacity);

    // 1. Start Consumer Threads
    Thread[] consumerThreads = new Thread[numConsumers];
    for (int i = 0; i < numConsumers; i++) {
      consumerThreads[i] = new Thread(new Consumer(queue, i + 1));
      consumerThreads[i].start();
    }

    // 2. Start Producer Thread
    Thread producerThread = new Thread(new Producer(queue, 1, 10));
    producerThread.start();

    try {
      // Wait for the producer to finish normally
      producerThread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.err.println("Main thread interrupted while waiting for producer.");
    } finally {
      // GUARANTEED CLEANUP: Even if the producer crashes or gets interrupted,
      // we inject the poison pills so the consumers never hang indefinitely.
      System.out.println("[Main] Producer finished or failed. Injecting poison pills...");
      try {
        for (int i = 0; i < numConsumers; i++) {
          queue.put(POISON_PILL);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.err.println("Main thread interrupted while injecting poison pills.");
      }
    }

    // 3. Wait for all consumers to finish processing
    try {
      for (int i = 0; i < numConsumers; i++) {
        consumerThreads[i].join();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    System.out.println("System shutdown complete.");
  }
}
