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
	"fmt"
	"github.com/proxy-wasm/proxy-wasm-go-sdk/proxywasm"
	"github.com/proxy-wasm/proxy-wasm-go-sdk/proxywasm/types"
)

// headerTests implements types.HttpContext.
type headerTests struct {
	// Embed the default http context here,
	// so that we don't need to reimplement all the methods.
	types.DefaultHttpContext
	contextID     uint32
	pluginContext *pluginContext
	counter       int
}

func (p *pluginContext) headerTests(contextID uint32) types.HttpContext {
	return &headerTests{
		contextID:     contextID,
		pluginContext: p,
	}
}

func (ctx *headerTests) OnHttpRequestHeaders(_ int, _ bool) types.Action {
	ctx.pluginContext.requestCounter++
	ctx.counter = ctx.pluginContext.requestCounter
	if err := proxywasm.AddHttpRequestHeader("x-request-counter", fmt.Sprintf("%d", ctx.counter)); err != nil {
		proxywasm.LogCriticalf("failed to set request counter header: %v", err)
	}
	return types.ActionContinue
}

// OnHttpResponseHeaders implements types.HttpContext.
func (ctx *headerTests) OnHttpResponseHeaders(_ int, _ bool) types.Action {

	if err := proxywasm.AddHttpResponseHeader("x-response-counter", fmt.Sprintf("%d", ctx.counter)); err != nil {
		proxywasm.LogCriticalf("failed to set response counter header: %v", err)
	}
	return types.ActionContinue
}
