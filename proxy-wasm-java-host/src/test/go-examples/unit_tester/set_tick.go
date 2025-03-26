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

// tickTests implements types.HttpContext.
type tickTests struct {
	types.DefaultHttpContext
	contextID     uint32
	pluginContext *pluginContext
}

func (
	p *pluginContext) tickTests(contextID uint32) types.HttpContext {
	return &tickTests{
		contextID:     contextID,
		pluginContext: p,
	}
}

func (ctx *tickTests) OnHttpRequestHeaders(int, bool) types.Action {
	pathBytes, err := proxywasm.GetProperty([]string{"request", "path"})
	if err != nil {
		proxywasm.LogCriticalf("failed to get :path : %v", err)
	}
	path := string(pathBytes)

	switch path {
	case "/tickTests/enable":
		if err := proxywasm.SetTickPeriodMilliSeconds(100); err != nil {
			proxywasm.LogCriticalf("failed to set tick period: %v", err)
			return types.ActionPause
		}
		if err := proxywasm.SendHttpResponse(200, [][2]string{}, []byte("ok"), -1); err != nil {
			proxywasm.LogCriticalf("failed to send local response: %v", err)
		}
		return types.ActionPause

	case "/tickTests/disable":
		if err := proxywasm.SetTickPeriodMilliSeconds(0); err != nil {
			proxywasm.LogCriticalf("failed to clear tick period: %v", err)
			return types.ActionPause
		}
		if err := proxywasm.SendHttpResponse(200, [][2]string{}, []byte("ok"), -1); err != nil {
			proxywasm.LogCriticalf("failed to send local response: %v", err)
		}
		return types.ActionPause
	case "/tickTests/get":
		if err := proxywasm.SendHttpResponse(200, [][2]string{}, []byte(fmt.Sprint(ctx.pluginContext.tickCounter)), -1); err != nil {
			proxywasm.LogCriticalf("failed to send local response: %v", err)
		}
		return types.ActionPause
	}

	return types.ActionContinue
}
