## Attribution

This example originally came from:
https://github.com/mosn/proxy-wasm-go-host/tree/main/example/data


### Build

Build the Docker image to be used to compile:

- `git clone https://github.com/proxy-wasm/proxy-wasm-cpp-sdk`
- `cd proxy-wasm-cpp-sdk`
- `docker build -f Dockerfile-sdk . -t wasmsdk:v3`

and finally, in this folder:

```bash
docker run -v $PWD:/work -w /work wasmsdk:v3 /build_wasm.sh http.wasm
```
