use std::sync::atomic::{AtomicU64, Ordering};
use std::sync::Arc;
use std::thread;
use std::time::Instant;

static COUNT: AtomicU64 = AtomicU64::new(0);

fn is_prime(n: u64) {
    let limit = n.isqrt();
    if n & 1 == 0 {
        return;
    }
    let mut i = 3;

    while i <= limit {
        if n % i == 0 {
            return;
        }
        i += 2
    }
    COUNT.fetch_add(1, Ordering::SeqCst);
}

fn multi_thread_counting(range_limit: u64) {
    let num_threads = 10;
    let next_number = Arc::new(AtomicU64::new(1));
    let start_time = Instant::now();
    let mut handles = vec![];
    for _ in 0..num_threads {
        let next_number = Arc::clone(&next_number);
        let handle = thread::spawn(move || loop {
            let n = next_number.fetch_add(1, Ordering::SeqCst);
            if n > range_limit {
                break;
            }
            is_prime(n);
        });
        handles.push(handle);
    }
    for handle in handles {
        handle.join().unwrap();
    }
}

fn count_single_thread(range_limit: u64) {
    for n in 1..=range_limit {
        is_prime(n);
    }
}

fn main() {
    let range_limit = 100000000;
    let start_time = Instant::now();
    //count_single_thread(range_limit);
    multi_thread_counting(range_limit);
    let final_count = COUNT.load(Ordering::SeqCst);
    let duration = start_time.elapsed();
    println!(
        "Found {} prime numbers up to {}. in time {:?}",
        final_count, range_limit, duration
    );
}
