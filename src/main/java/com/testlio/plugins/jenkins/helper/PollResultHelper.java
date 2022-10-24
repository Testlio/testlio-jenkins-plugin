package com.testlio.plugins.jenkins.helper;

import com.google.gson.Gson;
import com.testlio.plugins.jenkins.RunConfigurationAction;
import com.testlio.plugins.jenkins.dto.AutomatedRunResultsDTO;
import com.testlio.plugins.jenkins.dto.RunDTO;
import com.testlio.plugins.jenkins.utils.RestClient;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PollResultHelper {
  final static int MAX_FAILED_POLLS = 3;
  final static int POLLING_INTERVAL = 1000 * 60; // 1 minute
  final static int INITIAL_INTERVAL = 1000 * 180; // 3 minutes

  public static void pollResults(TaskListener listener, RestClient restClient, RunDTO automatedRun, RunConfigurationAction config) throws InterruptedException {
    if (!config.getWaitForResults()) {
      listener.getLogger().println("Polling is not configured, please check configuration to enable it");
      return;
    }

    listener.getLogger().println("Step 8.1: Initially Waiting for: " + INITIAL_INTERVAL / 1000 + " seconds, before result polling starts");
    Thread.sleep(INITIAL_INTERVAL);

    final String automatedRunResultHref = "/automated-test-run/v1/collections/" + config.getAutomatedTestRunCollectionGuid() + "/results-new?testRunHref=" + automatedRun.getHref();
    String resultStatus = "";
    int failedRequests = 0;
    listener.getLogger().println("Step 8.2: Starting to poll for results at an interval of " + POLLING_INTERVAL / 1000 + " seconds.");
    while (StringUtils.isBlank(resultStatus)) {
      ResponseEntity<String> response = restClient.getWithResponseEntity(automatedRunResultHref);

      listener.getLogger().println("ResponseCode:" + response.getStatusCode());
      if (response.getStatusCode() != HttpStatus.OK) {
        if (failedRequests >= MAX_FAILED_POLLS) {
          throw new Error("Failed to poll results, maximum tries reaches");
        }
        failedRequests++;
      }
      AutomatedRunResultsDTO resultResponse = new Gson().fromJson(response.getBody(), AutomatedRunResultsDTO.class);

      if (StringUtils.isNotBlank(resultResponse.getResults().getResult())) {
        resultStatus = resultResponse.getResults().getResult();
        break;
      }

      listener.getLogger().println("Polling - run still in progress");
      Thread.sleep(POLLING_INTERVAL);
    }

    if (StringUtils.isNotBlank(resultStatus)) {
      listener.getLogger().println("Polling - run finished with result: " + resultStatus);
      if (!StringUtils.equals(resultStatus, "PASSED")) {
        throw new Error("There are some failures in the result - Run Failed!!");
      }
    }
  }
}
