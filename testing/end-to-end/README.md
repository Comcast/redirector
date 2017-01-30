# Redirector End-To-End testing framework

End-To-End testing framework assumes Redirector WS and Redirector Gateway are deployed and End2End tool creates rules through Redirector WS and then 
performs requests to Redirector Gateway to retrieve redirects. Used as smoke test of new deployment.

Scenario of end-to-end testing:

1. E2E makes calls to WS for adding rules, distributions, lists etc. Expected results: 20* response codes, no errors
2. E2E registers proper stacks/hosts. 
3. E2E approves changes on WS. Expected result: changes are approved successfully
4. E2E makes API call to WS which returns list of test cases which will be used for testing of rules added in p.1
5. E2E transforms tests into Redirector Gateway requests and send them to Redirector Gateway
6. Once testing session is finished E2E calls report endpoint of Redirector Gateway and obtain actual results and logs related to tests being run
7. E2E compares actual results from p.6 with expected results from p.4 and generates testing report.  
8. Report is ready and cen be viewed on user-friendly report page by reaching E2E endpoint