package com.testlio.plugins.jenkins.utils;

import com.google.gson.Gson;
import hudson.model.TaskListener;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

public class RestClient {
  private static String BASE_URI = "https://api.testlio.net";
  private RestTemplate restTemplate;
  private HttpHeaders headers;

  private TaskListener listener;

  public RestClient(String accessToken, TaskListener listener) {
    this.restTemplate = new RestTemplate();
    this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(BASE_URI));
    this.headers = new HttpHeaders();
    this.listener = listener;
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(accessToken);
  }

  public <T> Object get(String url, Class<T> typeClass) {
    listener.getLogger().println("Making a GET Request, Request Url:" + url);
    ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    listener.getLogger().println("Response:" + response.getStatusCode());
    if (typeClass == null) {
      return null;
    }
    return new Gson().fromJson(response.getBody(), typeClass);
  }

  public <T> Object post(String url, JSONObject request, Class<T> typeClass) {
    listener.getLogger().println("Making a POST Request, Request Url:" + url);
    listener.getLogger().println("Request Payload:" + request.toString());
    ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.POST, new HttpEntity<>(request.toString(), headers), String.class);
    listener.getLogger().println("Response:" + response.getStatusCode());
    if (typeClass == null) {
      return null;
    }
    return new Gson().fromJson(response.getBody(), typeClass);
  }

  public <T> Object put(String url, JSONObject request, Class<T> typeClass) {
    listener.getLogger().println("Making a PUT Request, Request Url:" + url);
    listener.getLogger().println("Request Payload:" + request.toString());
    ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.PUT, new HttpEntity<>(request.toString(), headers), String.class);
    listener.getLogger().println("Response:" + response.getStatusCode());
    if (typeClass == null) {
      return null;
    }
    return new Gson().fromJson(response.getBody(), typeClass);
  }

  private RestTemplate getRestTemplate() {
    if (restTemplate == null) {
      restTemplate = new RestTemplate();
      restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(BASE_URI));
    }
    return restTemplate;
  }
}