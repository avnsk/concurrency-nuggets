use std::sync::atomic::{AtomicU64, Ordering};
use std::sync::mpsc;
use std::sync::Arc;
use std::thread;
use std::time::Instant;

static COUNT: AtomicU64 = AtomicU64::new(0);

fn is_prime(n: u64) -> bool {
    let limit = n.isqrt();
    if n & 1 == 0 {
        return false;
    }
    let mut i = 3;

    while i <= limit {
        if n % i == 0 {
            return false;
        }
        i += 2
    }
    true
}

fn multi_thread_counting_using_channel(range_limit: u64) -> u64 {
    let num_threads = 10;
    let mut handles = vec![];
    let next_number = Arc::new(AtomicU64::new(1));
    let (tx, rx) = mpsc::channel();
    for _ in 0..num_threads {
        let next_number = Arc::clone(&next_number);
        let tx = tx.clone();
        let handle = thread::spawn(move || loop {
            let n = next_number.fetch_add(1, Ordering::SeqCst);
            if n > range_limit {
                break;
            }
            if is_prime(n) {
                tx.send(()).unwrap();
            }
        });
        handles.push(handle);
    }
    drop(tx);
    let mut prime_count = 0;
    for _ in rx {
        prime_count += 1;
    }
    for handle in handles {
        handle.join().unwrap();
    }
    prime_count
}

fn multi_thread_counting(range_limit: u64) {
    let num_threads = 10;
    let next_number = Arc::new(AtomicU64::new(1));
    let mut handles = vec![];
    for _ in 0..num_threads {
        let next_number = Arc::clone(&next_number);
        let handle = thread::spawn(move || loop {
            let n = next_number.fetch_add(1, Ordering::SeqCst);
            if n > range_limit {
                break;
            }
            if is_prime(n) {
                COUNT.fetch_add(1, Ordering::SeqCst);
            }
        });
        handles.push(handle);
    }
    for handle in handles {
        handle.join().unwrap();
    }
}

fn count_single_thread(range_limit: u64) {
    for n in 1..=range_limit {
        if is_prime(n) {
            COUNT.fetch_add(1, Ordering::SeqCst);
        }
    }
}

fn main() {
    let range_limit = 100000000;
    let start_time = Instant::now();
    //count_single_thread(range_limit);
    // multi_thread_counting(range_limit);
    // let final_count = COUNT.load(Ordering::SeqCst);
    let final_count = multi_thread_counting_using_channel(range_limit);
    let duration = start_time.elapsed();
    println!(
        "Found {} prime numbers up to {}. in time {:?}",
        final_count, range_limit, duration
    );
}
