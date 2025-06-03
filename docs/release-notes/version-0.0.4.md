# Release Notes - Version 0.0.4

## Overview
Version 0.0.4 brings significant performance improvements, better memory efficiency, enhanced testing infrastructure, and important bug fixes to the Proxy-Wasm Java Host implementation.

## 🚀 New Features

### Optimizations
- **Lazy Request Body Loading**: Refactored request body handling so that it's not loaded into memory unless the WASM Pluign needs it
- **Lazy Response Body Loading**: Refactored response body handling so that it's not loaded into memory unless the WASM Pluign needs it.
- **Lazy Plugin Startup**: WASM modules now don't get started, until the a request needs to use the module.  

### Enhanced Testing Infrastructure
- **TomEE Integration Tests**: Added a comprehensive TomEE integration test module to ensure compatibility with TomEE application server
- **Improved Build Configuration**: Enhanced build setup and configuration management

### Runtime Upgrades
- **Chicory Runtime v1.4.0**: Upgraded to the latest Chicory WASM runtime for improved performance and stability

## 💥 Breaking Changes

- **Plugin Builder Relocation**: **BREAKING CHANGE** - Moved the builder from the `Plugin` class to the `PluginFactory` class. This change improves the API design but requires code updates:
  
  **Before:**
  ```java
  () -> Plugin.builder(module)
      .withName("waf")
      .build();
  ```
  
  **After:**
  ```java
  PluginFactory.builder(module)
      .withName("waf")
      .build();
  ```


## 🔧 Improvements

### Documentation
- **Usage Documentation**: Added comprehensive usage documentation to the README with examples and configuration guidance
- **CDI Configuration Examples**: Enhanced documentation for CDI-based plugin configuration

### Build System
- **Dependency Updates**: Updated multiple key dependencies for security and stability:
  - JUnit 5.12.0 → 5.13.0
  - Checkstyle 10.23.1 → 10.25.0
  - Maven Surefire Plugin 3.2.5 → 3.5.3
  - SLF4J 2.0.12 → 2.0.17
  - Jakarta Servlet API 6.0.0 → 6.1.0
  - Gson 2.12.1 → 2.13.1
  - Rest Assured 5.3.1 → 5.5.5
  - Spotless Maven Plugin 2.44.3 → 2.44.5
  - Jandex Maven Plugin 3.2.7 → 3.3.1
  - And several other minor dependency updates

---

For complete details, see the [full changelog](https://github.com/roastedroot/proxy-wasm-java-host/compare/0.0.3...0.0.4) on GitHub. 