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
	"strings"
)

func main() {}

type vmContext struct {
	types.DefaultVMContext
}

func init() {
	proxywasm.SetVMContext(&vmContext{})
}

// pluginContext implements types.PluginContext.
type pluginContext struct {
	types.DefaultPluginContext
	config         string
	newHttpContext func(contextID uint32) types.HttpContext
	requestCounter int
	tickCounter    int
}

func (*vmContext) NewPluginContext(contextID uint32) types.PluginContext {
	return &pluginContext{}
}

// OnPluginStart implements types.PluginContext.
func (p *pluginContext) OnPluginStart(pluginConfigurationSize int) types.OnPluginStartStatus {
	proxywasm.LogDebug("loading plugin config")
	data, err := proxywasm.GetPluginConfiguration()
	if data == nil {
		return types.OnPluginStartStatusOK
	}

	if err != nil {
		proxywasm.LogCriticalf("error reading plugin configuration: %v", err)
		return types.OnPluginStartStatusFailed
	}

	p.config = string(data)

	if !gjson.Valid(p.config) {
		proxywasm.LogCritical(`invalid configuration format; expected json`)
		return types.OnPluginStartStatusFailed
	}

	handlerType := strings.TrimSpace(gjson.Get(p.config, "type").Str)
	switch handlerType {
	case "headerTests":
		p.newHttpContext = p.headerTests
	case "tickTests":
		p.newHttpContext = p.tickTests
	case "httpCallTests":
		p.newHttpContext = p.httpCallTests
	case "ffiTests":
		p.newHttpContext = p.ffiTests
	default:
		proxywasm.LogCritical(`invalid config type"}`)
		return types.OnPluginStartStatusFailed
	}
	proxywasm.LogDebugf("using handlerType: %s", handlerType)

	return types.OnPluginStartStatusOK
}

// NewHttpContext implements types.PluginContext.
func (p *pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
	return p.newHttpContext(contextID)
}

// OnTick implements types.PluginContext.
func (ctx *pluginContext) OnTick() {
	ctx.tickCounter++
}
