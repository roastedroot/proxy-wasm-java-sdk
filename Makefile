#
# To build the Go examples, you need to have the Go compiler installed, and then run:
#
#    $ make build-go-examples
#
# To build the Rust examples, install rustup and cargo install the wasm target with run `rustup target add wasm32-wasip1`
# and then run:
#
#    $ make build-rust-examples
#

.PHONY: all
all: build-go-examples build-rust-examples

.PHONY: build-go-examples
build-go-examples:
	@find ./src/test/go-examples -mindepth 1 -type f -name "main.go" \
	| xargs -I {} bash -c 'dirname {}' \
	| xargs -I {} bash -c 'cd {} && env GOOS=wasip1 GOARCH=wasm go build -buildmode=c-shared -o main.wasm ./main.go'


.PHONY: build-rust-examples
build-rust-examples:
	@find ./src/test/rust-examples -mindepth 1  -type f -name "Cargo.toml" \
	| xargs -I {} bash -c 'dirname {}' \
	| xargs -I {} bash -c 'cd {} && cargo build --target wasm32-wasip1 --release; cp ./target/wasm32-wasip1/release/*.wasm ./main.wasm'
