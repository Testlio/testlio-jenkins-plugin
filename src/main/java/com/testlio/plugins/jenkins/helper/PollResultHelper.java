package com.testlio.plugins.jenkins.helper;

import com.testlio.plugins.jenkins.RunConfigurationAction;
import com.testlio.plugins.jenkins.dto.AutomatedRunResultsDTO;
import com.testlio.plugins.jenkins.dto.RunDTO;
import com.testlio.plugins.jenkins.utils.RestClient;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;

public class PollResultHelper {
  final static int MAX_FAILED_POLLS = 3;
  final static int POLLING_INTERVAL = 1000 * 60; // 1 minute
  final static int INITIAL_INTERVAL = 1000 * 240; // 4 minutes

  public static void pollResults(TaskListener listener, RestClient restClient, RunDTO automatedRun, RunConfigurationAction config) throws InterruptedException {
    if (!config.getWaitForResults()) {
      listener.getLogger().println("Polling is not configured, please check configuration to enable it");
      return;
    }

    listener.getLogger().println("Step 8.1: Initially Waiting for: "+INITIAL_INTERVAL/1000 +" seconds, before result pooling starts");
    Thread.sleep(INITIAL_INTERVAL);

    final String automatedRunResultHref = "/automated-test-run/v1/collections/" + config.getAutomatedTestRunCollectionGuid() + "/results-new?testRunHref=" + automatedRun.getHref();
    String resultStatus = null;
    int failedRequests = 0;
    listener.getLogger().println("Step 8.2: Starting to poll for results at an interval of " + POLLING_INTERVAL / 1000 + " seconds.");
    while (failedRequests <= MAX_FAILED_POLLS) {
      AutomatedRunResultsDTO resultResponse = (AutomatedRunResultsDTO) restClient.get(automatedRunResultHref, AutomatedRunResultsDTO.class);

      if (StringUtils.isNotBlank(resultResponse.getResults().getResult())) {
        resultStatus = resultResponse.getResults().getResult();
        break;
      }

      listener.getLogger().println("Polling - run still in progress");
      Thread.sleep(POLLING_INTERVAL);
      failedRequests++;
    }

    if (StringUtils.isNotBlank(resultStatus)) {
      listener.getLogger().println("Polling - run finished with result: " + resultStatus);
      if (!StringUtils.equals(resultStatus, "PASSED")) {
        throw new Error("There are some failures in the result - Run Failed!!");
      }
    } else {
      throw new Error("Failed to poll results!!");
    }
  }
}
