package com.testlio.plugins.jenkins.helper;

import com.testlio.plugins.jenkins.RunConfigurationAction;
import com.testlio.plugins.jenkins.dto.FileDTO;
import com.testlio.plugins.jenkins.dto.ProjectCollectionDTO;
import com.testlio.plugins.jenkins.dto.ResponseHrefDTO;
import com.testlio.plugins.jenkins.dto.UploadDTO;
import com.testlio.plugins.jenkins.utils.RestClient;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.Map;

public class UploadHelper {
  private static UploadDTO uploadToAWS(TaskListener listener, RestClient restClient, FileDTO uploadFile, String prefix) throws URISyntaxException {
    JSONObject uploadServiceRequest = new JSONObject();
    uploadServiceRequest.put("prefix", prefix);
    UploadDTO uploadDTO = (UploadDTO) restClient.post("/upload/v1/files", uploadServiceRequest, UploadDTO.class);
    RestTemplate restTemplate = new RestTemplate();
    listener.getLogger().println("Uploading file (" + uploadFile.getFileName() + ") to AWS");
    restTemplate.exchange(uploadDTO.getPut().getHref().toURI(), HttpMethod.PUT, new HttpEntity<>(new FileSystemResource(uploadFile.getFile())), Map.class);
    return uploadDTO;
  }

  public static void uploadTestPackage(TaskListener listener, RestClient restClient, FileDTO testPackage, ResponseHrefDTO automatedTestPlan, RunConfigurationAction config) throws URISyntaxException {
    listener.getLogger().println("Step 5.1: Get upload URL");
    UploadDTO uploadDTO = uploadToAWS(listener, restClient, testPackage, "automated-run-attachment");

    listener.getLogger().println("Step 5.2: Create new attachment");
    JSONObject createAttachmentRequest = new JSONObject();
    createAttachmentRequest.put("name", StringUtils.isNotBlank(testPackage.getFileName()) ? testPackage.getFileName() : "testPackage.zip");
    createAttachmentRequest.put("fileType", "application/zip");
    createAttachmentRequest.put("attachmentType", "TestPackage");
    createAttachmentRequest.put("url", uploadDTO.getGet().getHref().toURI().toString());
    createAttachmentRequest.put("size", testPackage.getFile().length());
    ResponseHrefDTO attachmentDTO = (ResponseHrefDTO) restClient.post("/automated-test-run/v1/collections/" + config.getAutomatedTestRunCollectionGuid() + "/attachments", createAttachmentRequest, ResponseHrefDTO.class);

    listener.getLogger().println("Step 5.4: Attach test package attachment to plan");
    JSONObject attachRequest = new JSONObject();
    attachRequest.put("attachmentHref", attachmentDTO.getHref());
    restClient.post(automatedTestPlan.getHref() + "/attachments", attachRequest, null);
  }

  public static void uploadBuild(TaskListener listener, RestClient restClient, FileDTO buildFile,
                                 ResponseHrefDTO automatedTestPlan, boolean isTestTypeDevice,
                                 RunConfigurationAction config, boolean isIOS) throws URISyntaxException {
    if (!isTestTypeDevice) {
      listener.getLogger().println("Skipping the step as no build is required for Web testing");
      return;
    }
    if (buildFile == null) {
      return;
    }
    listener.getLogger().println("Step 4.1: Get upload URL");
    UploadDTO uploadDTO = uploadToAWS(listener, restClient, buildFile, "build-v3");

    listener.getLogger().println("Step 4.3: Creating a new build");
    listener.getLogger().println("Getting build collection href");
    ProjectCollectionDTO projectCollectionDTO = (ProjectCollectionDTO) restClient.get("/project/v1/projects/" + config.getProjectId() + "/collection", ProjectCollectionDTO.class);
    listener.getLogger().println("Creating build");

    String createBuildUri = projectCollectionDTO.getBuildCollectionHref() + "/builds/file";
    if (isIOS) {
      createBuildUri += "?resign=true";
    }
    JSONObject createBuildRequest = new JSONObject();
    createBuildRequest.put("name", buildFile.getFileName());
    createBuildRequest.put("url", uploadDTO.getGet().getHref().toURI().toString());
    createBuildRequest.put("version", "1.0.0");
    ResponseHrefDTO createBuildResponse = (ResponseHrefDTO) restClient.post(createBuildUri, createBuildRequest, ResponseHrefDTO.class);

    listener.getLogger().println("Step 4.4: Attach build to plan");
    JSONObject attachBuildRequest = new JSONObject();
    attachBuildRequest.put("buildHref", createBuildResponse.getHref());
    attachBuildRequest.put("useOriginal", true);
    restClient.post(automatedTestPlan.getHref() + "/builds", attachBuildRequest, null);
  }

}
