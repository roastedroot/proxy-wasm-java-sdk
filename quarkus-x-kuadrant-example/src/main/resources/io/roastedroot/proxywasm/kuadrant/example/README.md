## Attribution

The wasm_shim.wasm plugin comes from:

https://github.com/Kuadrant/wasm-shim

add to `Cargo.toml`:

```toml
[profile.release]
opt-level = "s"
debug = false
```

to build run:

```bash
cargo build --release --target wasm32-unknown-unknown
```
