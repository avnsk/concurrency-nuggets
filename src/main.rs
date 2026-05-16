use std::sync::atomic::{AtomicU64, Ordering};
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

fn count_single_thread(range_limit: u64) {
    for n in 1..=range_limit {
        is_prime(n);
    }
}

fn main() {
    let range_limit = 100000000;
    let start_time = Instant::now();
    count_single_thread(range_limit);
    let final_count = COUNT.load(Ordering::SeqCst);
    let duration = start_time.elapsed();
    println!(
        "Found {} prime numbers up to {}. in time {:?}",
        final_count, range_limit, duration
    );
}
