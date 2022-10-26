# testlio-jenkins-plugin

## Introduction

Create automated Testlio runs in your CI pipeline.

## Getting started

In order to use the Testlio jenkins plugin you will need to create and provide Run API token. To find out how to obtain it, check [How to create an automated run with API](https://help.testlio.com/en/articles/5898210-how-to-create-an-automated-run-with-api)

After creating Run API Token on Testlio Platform, please follow below steps

- Go to Manage Jenkins-> Configure System -> Global properties section and enable `Enviornment Variables` and add a new env variable `testlioToken` and set the token value and save
![](../../../../../../../var/folders/6g/2dc5_ysx6p93xnjwj69kgt080000gn/T/TemporaryItems/NSIRD_screencaptureui_mNzSOB/Screenshot 2022-10-26 at 11.47.39 AM.png)

- Go to the project configuration
- Click Add build step 
- Select `Schedule automated run on Testlio`![](../../../../../../../var/folders/6g/2dc5_ysx6p93xnjwj69kgt080000gn/T/TemporaryItems/NSIRD_screencaptureui_Mhr2hp/Screenshot 2022-10-26 at 11.49.43 AM.png)
- `Specify your Testlio Workspace ID` value can be found from `Run Public API` Testlio Platform
- `Specify your test run collection GUID` value can be found from `Run Public API` Testlio Platform
- `Specify your automated test run collection GUID` value can be found from `Run Public API` Testlio Platform
- In `Choose your application` select the application type (Mobile App, Mobile Web, Desktop Web)
- In `Specify your app build URL` please provide a public downloadable URL of your application build 
- In `Configure your tests` please select the test type
- In `Specify your test package URL` please provide a public downloadable URL of your test package
- Please select browsers/devices that you want to schedule for the run
- In `Advance Settings` there is an option `Wait for results` this is by default checked, after your tests are run this will wait to poll results from Testlio Platform ![](../../../../../../../var/folders/6g/2dc5_ysx6p93xnjwj69kgt080000gn/T/TemporaryItems/NSIRD_screencaptureui_hTXc42/Screenshot 2022-10-26 at 11.58.11 AM.png)  
- Save the settings and you can now trigger `build now`

## Issues

Please open an issue on github if you find something, will be happy to look into it.

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

