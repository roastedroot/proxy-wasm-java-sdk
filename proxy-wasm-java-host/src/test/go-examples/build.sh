#!/bin/sh

docker run -it --rm \
  -v `pwd`:/src \
  --workdir /src \
  -e GOOS=wasip1 \
  -e GOARCH=wasm golang:1.24-alpine sh -c "
    find . -mindepth 1 -type f -name 'main.go' \
    | xargs -I {} sh -c 'dirname {}' \
    | xargs -I {} sh -c 'cd {} && GOOS=wasip1 GOARCH=wasm go build -buildmode=c-shared -o main.wasm ./main.go'
  "