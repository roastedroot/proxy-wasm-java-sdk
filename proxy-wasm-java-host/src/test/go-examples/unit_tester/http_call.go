// Copyright 2020-2024 Tetrate
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package main

import (
	"github.com/proxy-wasm/proxy-wasm-go-sdk/proxywasm"
	"github.com/proxy-wasm/proxy-wasm-go-sdk/proxywasm/types"
	"github.com/tidwall/gjson"
)

// httpCallTests implements types.HttpContext.
type httpCallTests struct {
	types.DefaultHttpContext
	contextID       uint32
	pluginContext   *pluginContext
	headers         [][2]string
	responseHeaders [][2]string
	responseBody    []byte
}

func (
	p *pluginContext) httpCallTests(contextID uint32) types.HttpContext {
	return &httpCallTests{
		DefaultHttpContext: types.DefaultHttpContext{},
		contextID:          contextID,
		pluginContext:      p,
		headers:            nil,
	}
}

func (ctx *httpCallTests) OnHttpRequestHeaders(int, bool) types.Action {
	proxywasm.LogDebug("OnHttpRequestHeaders")
	var err error
	ctx.headers, err = proxywasm.GetHttpRequestHeaders()
	if err != nil {
		proxywasm.LogCriticalf("failed to get request headers: %v", err)
	}

	method, err := proxywasm.GetProperty([]string{"request", "method"})
	if err != nil {
		proxywasm.LogCriticalf("failed to get request method: %v", err)
	}
	ctx.headers = append(ctx.headers, [2]string{":method", string(method)})

	host, err := proxywasm.GetProperty([]string{"request", "host"})
	if err != nil {
		proxywasm.LogCriticalf("failed to get request host: %v", err)
	}
	ctx.headers = append(ctx.headers, [2]string{":authority", string(host)})

	path := gjson.Get(ctx.pluginContext.config, "path").Str
	ctx.headers = append(ctx.headers, [2]string{":path", path})

	return types.ActionContinue
}

func (ctx *httpCallTests) OnHttpRequestBody(bodySize int, endOfStream bool) types.Action {
	proxywasm.LogDebug("OnHttpRequestBody")
	if !endOfStream {
		// Wait until we see the entire body to replace.
		return types.ActionPause
	}

	body := []byte{}
	var err error
	if bodySize != 0 {
		body, err = proxywasm.GetHttpRequestBody(0, bodySize)
		if err != nil {
			proxywasm.LogErrorf("failed to get request body: %v", err)
			return types.ActionContinue
		}
	}

	upstream := gjson.Get(ctx.pluginContext.config, "upstream").Str
	if upstream == "" {
		upstream = "upstream"
	}

	proxywasm.LogDebug("DispatchHttpCall")
	_, err = proxywasm.DispatchHttpCall(upstream, ctx.headers, body, [][2]string{}, 5000, func(numHeaders, bodySize, numTrailers int) {

		proxywasm.LogDebug("On DispatchHttpCall Response")
		var err error
		ctx.responseHeaders, err = proxywasm.GetHttpCallResponseHeaders()
		if err != nil {
			proxywasm.LogCriticalf("failed to get response headers: %v", err)
		}

		ctx.responseBody, err = proxywasm.GetHttpCallResponseBody(0, bodySize)
		if err != nil {
			proxywasm.LogCriticalf("failed to get response body: %v", err)
		}

		err = proxywasm.SendHttpResponse(200, ctx.responseHeaders, ctx.responseBody, -1)
		if err != nil {
			proxywasm.LogCriticalf("failed to send local response: %v", err)
		}

	})
	if err != nil {
		proxywasm.LogCriticalf("failed to dispatch http call: %v", err)
		return types.ActionContinue
	}

	// Pause the stream to wait for the http call response.
	return types.ActionPause
}
