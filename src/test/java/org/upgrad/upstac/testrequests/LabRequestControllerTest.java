package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.users.User;
import org.upgrad.upstac.users.UserService;
import org.upgrad.upstac.users.models.Gender;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@Slf4j
class LabRequestControllerTest {


    @Autowired
    LabRequestController labRequestController;




    @Autowired
    TestRequestQueryService testRequestQueryService;

    /**
     * Added this property to prepare the test data in actual in-memory H2 DB since I cannot change above annotations
     * to mock as per grading criteria: Don't change the provided stub
     * Therefore changing style of testing to component integration testing
     */
    @Autowired
    TestRequestService testRequestService;
    @Autowired
    UserService userService;
    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * Since editing stub is against grading guidelines, and test cases will fail as data is not prepared,
     * beforeEach will prepare the data in the in-memory H2 database
     */
    @BeforeEach
    public void setUp() {

        // appInitializationService.initialize();
        // tried reflection to override userLoggedInService and inject it to Controller, but is not required.
        // Thanks to initialise method above which run on app start event
        /**
         * Create test request from users account
         * Create a lab_in_progress entry in DB
         */
        // Cleanup: Required if some corrupted data exists. Clean up is not usually reqd if we running test from scratch
        // But in case we arent and we are suing persistent H2, cleanup maybe required
        destroy();

        User user = userService.findByUserName("user");
        CreateTestRequest createTestRequest = new CreateTestRequest();
        createTestRequest.setAddress("some Addres");
        createTestRequest.setAge(98);
        createTestRequest.setEmail("someone" + "123456789" + "@somedomain.com");
        createTestRequest.setGender(Gender.MALE);
        createTestRequest.setName("someuser");
        createTestRequest.setPhoneNumber("123456789");
        createTestRequest.setPinCode(716768);

        try{
            /** Create sample testRequest */
            testRequestService.createTestRequestFrom(user, createTestRequest);
        }catch(Exception e) {
            System.out.println("User already exists. Will continue");
        }
        try{
            /** Create sample testRequest and change status of it to INITIATED */
            createTestRequest.setEmail(createTestRequest.getEmail()+ "m");
            createTestRequest.setPhoneNumber(createTestRequest.getPhoneNumber()+ "0");
            TestRequest testRequest = testRequestService.createTestRequestFrom(user, createTestRequest);
            labRequestController.assignForLabTest(testRequest.getRequestId());
        }catch(Exception e){
            System.out.println("User already exists. Will continue");
        }
    }

    @AfterEach
    public void destroy(){
        /**
         * I don't have delete method for test_request_flow table and cant change code in app to add delete request in interface
         * So I go low level and delete entries from DB directly...
         * Since this is test case, there will be no additional entries. Can safely truncate the tables, and delete all entries in
         * test_request table.
         */
        try{
            namedParameterJdbcTemplate.update("TRUNCATE TABLE CONSULTATION RESTART IDENTITY", new HashMap<>());
            namedParameterJdbcTemplate.update("TRUNCATE TABLE LAB_RESULT RESTART IDENTITY", new HashMap<>());
            namedParameterJdbcTemplate.update("TRUNCATE TABLE TEST_REQUEST_FLOW RESTART IDENTITY", new HashMap<>());
            namedParameterJdbcTemplate.update("DELETE FROM TEST_REQUEST", new HashMap<>());
        }catch(Exception e){
            System.out.println("Exception occurred in truncating table. Will continue: "+ e.getMessage());
        }
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.INITIATED);
        //Implement this method

        //Create another object of the TestRequest method and explicitly assign this object for Lab Test using assignForLabTest() method
        // from labRequestController class. Pass the request id of testRequest object.

        //Use assertThat() methods to perform the following two comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'LAB_TEST_IN_PROGRESS'
        // make use of assertNotNull() method to make sure that the lab result of second object is not null
        // use getLabResult() method to get the lab result

        TestRequest assignedRequest = labRequestController.assignForLabTest(testRequest.getRequestId());
        assertThat(testRequest.getRequestId(), equalTo(assignedRequest.getRequestId()));
        assertThat(assignedRequest.getStatus(), equalTo(RequestStatus.LAB_TEST_IN_PROGRESS));
        assertNotNull(assignedRequest.getLabResult());
    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception(){

        Long InvalidRequestId= -34L;

        //Implement this method


        // Create an object of ResponseStatusException . Use assertThrows() method and pass assignForLabTest() method
        // of labRequestController with InvalidRequestId as Id


        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            labRequestController.assignForLabTest(InvalidRequestId);
        });

        assertThat(ex.getMessage(), containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

        //Implement this method
        //Create an object of CreateLabResult and call getCreateLabResult() to create the object. Pass the above created object as the parameter

        //Create another object of the TestRequest method and explicitly update the status of this object
        // to be 'LAB_TEST_IN_PROGRESS'. Make use of updateLabTest() method from labRequestController class (Pass the previously created two objects as parameters)

        //Use assertThat() methods to perform the following three comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'LAB_TEST_COMPLETED'
        // 3. the results of both the objects created should be same. Make use of getLabResult() method to get the results.

        CreateLabResult labResult = getCreateLabResult(testRequest);
        TestRequest updatedRequest = labRequestController.updateLabTest(testRequest.getRequestId(), labResult);
        assertThat(updatedRequest.getRequestId(), equalTo(testRequest.getRequestId()));
        assertThat(updatedRequest.getStatus(), equalTo(RequestStatus.LAB_TEST_COMPLETED));
        assertThat(updatedRequest.getLabResult().getResult(), equalTo(labResult.getResult()));
    }


    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);


        //Implement this method

        //Create an object of CreateLabResult and call getCreateLabResult() to create the object. Pass the above created object as the parameter

        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateLabTest() method
        // of labRequestController with a negative long value as Id and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method


        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"

        CreateLabResult labResult = getCreateLabResult(testRequest);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            labRequestController.updateLabTest(-1L, labResult);
        });

        assertThat(ex.getMessage(), containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

        //Implement this method

        //Create an object of CreateLabResult and call getCreateLabResult() to create the object. Pass the above created object as the parameter
        // Set the result of the above created object to null.

        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateLabTest() method
        // of labRequestController with request Id of the testRequest object and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method


        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "ConstraintViolationException"

        CreateLabResult labResult = getCreateLabResult(testRequest);
        /**
         * calling_updateLabTest_with_invalid_empty_status_should_throw_exception
         * Status is set internally and not via controller. I am assuming status here means Result
         */
        labResult.setResult(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            labRequestController.updateLabTest(testRequest.getRequestId(), labResult);
        });

        assertThat(ex.getMessage(), containsString("ConstraintViolationException"));

    }

    public CreateLabResult getCreateLabResult(TestRequest testRequest) {

        //Create an object of CreateLabResult and set all the values
        // Return the object

        String bloodPressure, heartBeat, temperature, oxygenLevel, comments = "";
        bloodPressure = StringUtils.isEmpty(testRequest.getLabResult().getBloodPressure())? "80": testRequest.getLabResult().getBloodPressure();
        heartBeat = StringUtils.isEmpty(testRequest.getLabResult().getHeartBeat())? "70": testRequest.getLabResult().getHeartBeat();
        temperature = StringUtils.isEmpty(testRequest.getLabResult().getTemperature())? "97": testRequest.getLabResult().getTemperature();
        oxygenLevel = StringUtils.isEmpty(testRequest.getLabResult().getOxygenLevel())? "99": testRequest.getLabResult().getOxygenLevel();
        comments = StringUtils.isEmpty(testRequest.getLabResult().getComments())? "Reports are fine": testRequest.getLabResult().getComments();
        TestStatus status = testRequest.getLabResult().getResult() == null ? TestStatus.NEGATIVE : testRequest.getLabResult().getResult();

        CreateLabResult labResult = new CreateLabResult();
        labResult.setBloodPressure(bloodPressure);
        labResult.setHeartBeat(heartBeat);
        labResult.setTemperature(temperature);
        labResult.setOxygenLevel(oxygenLevel);
        labResult.setComments(comments);
        labResult.setResult(status);

        return labResult; // Replace this line with your code
    }

}