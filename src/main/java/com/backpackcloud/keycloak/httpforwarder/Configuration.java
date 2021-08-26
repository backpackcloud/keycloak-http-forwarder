/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Marcelo Guimarães
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.backpackcloud.keycloak.httpforwarder;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {

  private int timeout;
  private String url;
  private Map<String, String> headers;

  public Configuration() {
    this.timeout = Integer.parseInt(System.getProperty("events.http.timeout", "2"));
    this.url = System.getProperty("events.http.url", "http://localhost:8000");
    this.headers = new HashMap<>();
    Properties properties = System.getProperties();
    String headerPrefix = "events.http.header.";
    properties.stringPropertyNames().stream()
      .filter(name -> name.startsWith(headerPrefix))
      .forEach(header -> headers.put(header.substring(headerPrefix.length()), properties.getProperty(header)));
  }

  public int timeout() {
    return timeout;
  }

  public String url() {
    return url;
  }

  public Map<String, String> headers() {
    return headers;
  }

}
