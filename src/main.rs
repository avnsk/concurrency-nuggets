use std::sync::atomic::{AtomicU64, Ordering};

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
        i += 1
    }
    COUNT.fetch_add(1, Ordering::SeqCst);
}

fn main() {
    let range_limit = 100;

    for n in 1..=range_limit {
        is_prime(n);
    }
    let final_count = COUNT.load(Ordering::SeqCst);

    println!("Found {} prime numbers up to {}.", final_count, range_limit);
}
