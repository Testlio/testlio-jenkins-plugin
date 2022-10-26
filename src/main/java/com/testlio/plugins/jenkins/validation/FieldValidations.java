package com.testlio.plugins.jenkins.validation;

import com.testlio.plugins.jenkins.dto.FileDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

public class FieldValidations {

  static final Double ONE_GB_IN_BYTES = Math.pow(1024, 3);
  static final Double FILE_MAX_SIZE = 4 * ONE_GB_IN_BYTES;

  public static boolean validateAccessToken(String accessToken) {
    if (StringUtils.isBlank(accessToken)) {
      throw new IllegalArgumentException("Access token is missing, please configure it in your global configuration");
    }
    return true;
  }

  public static FileDTO downloadFileFromURL(String URL) {
    RestTemplate restTemplate = new RestTemplate();
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setBufferRequestBody(false);
    restTemplate.setRequestFactory(requestFactory);
    RequestCallback requestCallback = request -> request
            .getHeaders()
            .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));


    final FileDTO fileDTO = new FileDTO();
    File file = restTemplate.execute(URL, HttpMethod.GET, requestCallback, clientHttpResponse -> {
      fileDTO.setFileName(clientHttpResponse.getHeaders().getContentDisposition().getFilename());
      File ret = File.createTempFile("prefix-", "-suffix");
      ret.deleteOnExit();
      StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
      return ret;
    });

    if(file!=null && file.length() > FILE_MAX_SIZE) {
      throw new IllegalArgumentException("Uploading "+fileDTO.getFileName()+" failed: max. file size "+FILE_MAX_SIZE/ONE_GB_IN_BYTES+"GB");
    }
    fileDTO.setFile(file);
    return fileDTO;
  }


}
