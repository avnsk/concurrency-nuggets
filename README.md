# Concurrent Rust Exercises

A collection of multi-threaded and concurrent programming exercises implemented in Rust to demonstrate performance optimization, synchronization primitives, and parallel computing.

## Exercises

### 1. Multi-Threaded Prime Counter
This exercise benchmarks the speed difference between checking for prime numbers sequentially on a single thread versus distributing the workload dynamically across multiple threads.

#### Implementation Details
* **Workload Distribution:** Threads dynamically pull the next available number from a shared `AtomicU64` counter.
* **Synchronization:** Uses standard library atomics (`AtomicU64`) and memory orderings (`Ordering::SeqCst`) to safely increment values across threads without mutex locks.
* **Primality Test:** Uses a basic trial division algorithm optimized to check odd numbers up to the square root ($\sqrt{n}$) of the target number.

#### Benchmarks
The following results were recorded on a local machine searching for all prime numbers up to **100,000,000**:


| Execution Mode | Threads | Execution Time | Speedup |
| :--- | :--- | :--- | :--- |
| **Single-Threaded** | 1 | 40.51 seconds | Baseline (1x) |
| **Multi-Threaded** | 10 | 9.59 seconds | ~4.22x faster |

*Note: Total primes found in both runs: 5,761,455.*

### 2.Mutltithreaded TCP Server
Basic TCP server showing multithreaded nature using OS threads. Generally [Tokio] (https://tokio.rs/) is used for these which gives tasks which are similar to goroutines and are really lightweight
