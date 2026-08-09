package com.example;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CustomThreadPool {
  private final BlockingQueue<Runnable> taskQueue;
  private final WorkerThread[] workers;
  private volatile boolean isShutdown = false;

  public CustomThreadPool(int queueSize, int numberOfThreads) {
    this.taskQueue = new ArrayBlockingQueue<Runnable>(queueSize);
    this.workers = new WorkerThread[numberOfThreads];
    for (int i = 0; i < numberOfThreads; i++) {
      this.workers[i] = new WorkerThread();
      this.workers[i].start();
    }
  }

  public synchronized void submit(Runnable task) {
    if (isShutdown) {
      throw new IllegalStateException("ThreadPool is stopped. Cannot accept tasks.");
    }
    taskQueue.add(task);
  }

  public synchronized void shutdown() {
    this.isShutdown = true;
    for (WorkerThread t : workers) {
      t.interrupt();
    }
  }

  private class WorkerThread extends Thread {
    @Override
    public void run() {
      while (!isShutdown || !taskQueue.isEmpty()) {
        try {
          Runnable task = taskQueue.take();
          task.run();
        } catch (InterruptedException e) {
          if (isShutdown && taskQueue.isEmpty()) {
            break;
          }
        }
      }
    }
  }
}
