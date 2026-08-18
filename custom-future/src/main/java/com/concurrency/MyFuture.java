package com.concurrency;

import java.util.concurrent.Callable;

public class MyFuture<V> implements Runnable {

  private Object result;
  private final Callable<V> callable;

  private enum State {
    NEW,
    COMPLETING,
    NORMAL,
    EXCEPTIONAL
  }

  private volatile State state = State.NEW;

  public MyFuture(Callable<V> callable) {
    this.callable = callable;
  }

  @Override
  public void run() {
    /***
     * This if we see seems redundant, but it is a preformnance optimization to avoid acquiring
     * the lock if the state is not NEW. the thread reads this.state before acquiring the lock.
     * This is a performance optimization (a "balking check") to avoid the heavy cost of acquiring
     * a monitor lock if the task has already started or finished.
     ***/

    if (this.state != State.NEW) {
      return;
    }
    synchronized (this) {
      if (this.state != State.NEW) {
        return;
      }
      this.state = State.COMPLETING;
    }

    try {
      V computedResult = callable.call();
      synchronized (this) {
        this.result = computedResult;
        this.state = State.NORMAL;
        notifyAll();
      }
    } catch (Throwable ex) {
      synchronized (this) {
        this.result = ex;
        this.state = State.EXCEPTIONAL;
        notifyAll();
      }
    }
  }

  public V get() throws Exception {
    synchronized (this) {
      while (state == State.NEW || state == State.COMPLETING) {
        wait();
      }

      if (state == State.EXCEPTIONAL) {
        throw new Exception((Throwable) result);
      }
      return (V) result;
    }
  }
}
