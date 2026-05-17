use std::net::{TcpListener, TcpStream};
use std::thread;

fn handle_client(_: TcpStream) {
    println!("processing completed");
}

fn main() {
    let listener = TcpListener::bind("127.0.0.1:8080").unwrap();
    println!("Listening on :8080");
    for stream in listener.incoming() {
        match stream {
            Ok(stream) => {
                println!("New client connected");
                thread::spawn(|| {
                    handle_client(stream);
                });
            }
            Err(e) => {
                eprintln!("Connection failed: {}", e);
            }
        }
    }
}
