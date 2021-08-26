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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpForwarder implements EventListenerProvider {

  private static final Logger LOGGER = Logger.getLogger(HttpForwarder.class);

  // The event listener will block the regular Keycloak flow so let's use an executor service for better throughput
  private final ExecutorService executorService;

  private final HttpClient client;
  private final ObjectMapper objectMapper;
  private final HttpRequest.Builder requestBuilder;

  public HttpForwarder(Configuration configuration) {
    this.client = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(Duration.ofSeconds(configuration.timeout()))
      .followRedirects(HttpClient.Redirect.NORMAL)
      .build();

    this.objectMapper = new ObjectMapper();

    this.executorService = Executors.newCachedThreadPool();

    this.requestBuilder = HttpRequest.newBuilder(URI.create(configuration.url()));

    configuration.headers().forEach(requestBuilder::header);

    this.requestBuilder.header("Content-Type", "application/json");
  }

  @Override
  public void onEvent(Event event) {
    LOGGER.debugf("Received event [%s] from %s", event.getType(), event.getClientId());
    send(event);
  }

  @Override
  public void onEvent(AdminEvent adminEvent, boolean b) {
    LOGGER.debugf("Received admin event [%s] targeting %s", adminEvent.getOperationType(), adminEvent.getResourcePath());
    send(adminEvent);
  }

  private void send(Object event) {
    executorService.submit(() -> {
      try {
        HttpRequest request = this.requestBuilder.copy()
          .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(event)))
          .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400 && response.statusCode() < 600) {
          LOGGER.errorf("HTTP endpoint returned [%d] : %s", response.statusCode(), response.body());
        }
      } catch (IOException | InterruptedException e) {
        LOGGER.error("Error while forwarding event", e);
      }
    });
  }

  @Override
  public void close() {

  }

  public void shutdown() {
    LOGGER.info("Shutting down executor service");
    executorService.shutdown();
    try {
      executorService.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOGGER.error("Error while shutting down the executor service", e);
    }
  }

}