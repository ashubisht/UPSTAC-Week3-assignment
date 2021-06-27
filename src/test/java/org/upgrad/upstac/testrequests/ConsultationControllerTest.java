package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.consultation.ConsultationController;
import org.upgrad.upstac.testrequests.consultation.CreateConsultationRequest;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
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
class ConsultationControllerTest {


    @Autowired
    ConsultationController consultationController;


    @Autowired
    TestRequestQueryService testRequestQueryService;


    /**
     * Added this property to prepare the test data in actual in-memory H2 DB since I cannot change above annotations
     * to mock as per grading criteria: Don't change the provided stub
     * Therefore changing style of testing to component integration testing
     */
    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    /**
     * Since editing stub is against grading guidelines, and test cases will fail as data is not prepared,
     * beforeAll will prepare the data in the in-memory H2 database
     * Prepare test data for
     * 1. Lab Test complete
     * 2. Diagnosis in process
     * 3. Another diagnosis in process
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

        // Preparing lab test data is bit complicated. So directly using insert statements.
        // anyways I have to use jdbc template to delete from db

        try{

            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource sqlParameterSource = new MapSqlParameterSource();

            namedParameterJdbcTemplate.update("INSERT INTO PUBLIC.TEST_REQUEST " +
                            "( ADDRESS, AGE, CREATED, EMAIL, GENDER, NAME, PHONE_NUMBER, PIN_CODE, STATUS, CREATED_BY_ID) VALUES " +
                            "( 'some Addres', 98, '2021-06-27', 'someone123456789@somedomain.com', 0, 'someuser', '123456789', 716768, 2, 1)",
                    sqlParameterSource, keyHolder
            );
            int id= keyHolder.getKey().intValue();
            ((MapSqlParameterSource)sqlParameterSource).addValue("id", id);
            namedParameterJdbcTemplate.update("INSERT INTO PUBLIC.TEST_REQUEST_FLOW " +
                            "( FROM_STATUS, HAPPENED_ON, TO_STATUS, CHANGED_BY_ID, REQUEST_REQUEST_ID) VALUES ( 0, '2021-06-27', 1, 3, :id);",
                    sqlParameterSource
            );

            namedParameterJdbcTemplate.update("INSERT INTO PUBLIC.TEST_REQUEST_FLOW " +
                            "( FROM_STATUS, HAPPENED_ON, TO_STATUS, CHANGED_BY_ID, REQUEST_REQUEST_ID) VALUES ( 0, '2021-06-27', 2, 3, :id);",
                    sqlParameterSource
            );

            namedParameterJdbcTemplate.update("INSERT INTO PUBLIC.LAB_RESULT " +
                            "( BLOOD_PRESSURE, COMMENTS, HEART_BEAT, OXYGEN_LEVEL, RESULT, TEMPERATURE, UPDATED_ON, " +
                            "REQUEST_REQUEST_ID, TESTER_ID) VALUES " +
                            "( '102', 'Should be left', '97', '95', 0, '98', '2021-06-27', :id, 3);",
                    sqlParameterSource
            );

        }catch(Exception e){
            System.out.println("user 1 data exist");
        }

        try{
            // 2nd value entry
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource sqlParameterSource = new MapSqlParameterSource();

            namedParameterJdbcTemplate.update("INSERT INTO PUBLIC.TEST_REQUEST " +
                            "( ADDRESS, AGE, CREATED, EMAIL, GENDER, NAME, PHONE_NUMBER, PIN_CODE, STATUS, CREATED_BY_ID) VALUES " +
                            "( 'some Addres', 98, '2021-06-27', 'someone123456789@somedomain.comm', 0, 'someuser', '1234567890', 716768, 3, 1)",
                    sqlParameterSource, keyHolder
            );
            int id= keyHolder.getKey().intValue();
            ((MapSqlParameterSource)sqlParameterSource).addValue("id", id);
            namedParameterJdbcTemplate.update("INSERT INTO PUBLIC.TEST_REQUEST_FLOW " +
                            "( FROM_STATUS, HAPPENED_ON, TO_STATUS, CHANGED_BY_ID, REQUEST_REQUEST_ID) VALUES ( 0, '2021-06-27', 1, 3, :id);",
                    sqlParameterSource
            );

            namedParameterJdbcTemplate.update("INSERT INTO PUBLIC.TEST_REQUEST_FLOW " +
                            "( FROM_STATUS, HAPPENED_ON, TO_STATUS, CHANGED_BY_ID, REQUEST_REQUEST_ID) VALUES ( 0, '2021-06-27', 2, 3, :id);",
                    sqlParameterSource
            );

            namedParameterJdbcTemplate.update("INSERT INTO PUBLIC.LAB_RESULT " +
                            "( BLOOD_PRESSURE, COMMENTS, HEART_BEAT, OXYGEN_LEVEL, RESULT, TEMPERATURE, UPDATED_ON, " +
                            "REQUEST_REQUEST_ID, TESTER_ID) VALUES " +
                            "( '102', 'Should be left', '97', '95', 0, '98', '2021-06-27', :id, 3);",
                    sqlParameterSource
            );
            namedParameterJdbcTemplate.update("INSERT INTO PUBLIC.CONSULTATION " +
                            "(ID, COMMENTS, SUGGESTION, UPDATED_ON, DOCTOR_ID, REQUEST_REQUEST_ID) " +
                            "VALUES (1, null, null, null, 2, :id);",
                    sqlParameterSource
            );

        }catch (Exception e){
            System.out.println("User 2 data exists");
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
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_COMPLETED);

        //Implement this method

        //Create another object of the TestRequest method and explicitly assign this object for Consultation using assignForConsultation() method
        // from consultationController class. Pass the request id of testRequest object.

        //Use assertThat() methods to perform the following two comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'DIAGNOSIS_IN_PROCESS'
        // make use of assertNotNull() method to make sure that the consultation value of second object is not null
        // use getConsultation() method to get the lab result

        TestRequest assignedRequest = consultationController.assignForConsultation(testRequest.getRequestId());
        assertThat(testRequest.getRequestId(), equalTo(assignedRequest.getRequestId()));
        assertThat(assignedRequest.getStatus(), equalTo(RequestStatus.DIAGNOSIS_IN_PROCESS));
        assertNotNull(assignedRequest.getConsultation());

    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_throw_exception(){

        Long InvalidRequestId= -34L;

        //Implement this method


        // Create an object of ResponseStatusException . Use assertThrows() method and pass assignForConsultation() method
        // of consultationController with InvalidRequestId as Id


        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, ()->{
            consultationController.assignForConsultation(InvalidRequestId);
        });

        assertThat(ex.getMessage(), containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        //Implement this method
        //Create an object of CreateConsultationRequest and call getCreateConsultationRequest() to create the object. Pass the above created object as the parameter

        //Create another object of the TestRequest method and explicitly update the status of this object
        // to be 'COMPLETED'. Make use of updateConsultation() method from consultationController class
        // (Pass the previously created two objects as parameters)
        // (for the object of TestRequest class, pass its ID using getRequestId())

        //Use assertThat() methods to perform the following three comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'COMPLETED'
        // 3. the suggestion of both the objects created should be same. Make use of getSuggestion() method to get the results.

        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
        TestRequest updatedRequest = consultationController.updateConsultation(testRequest.getRequestId(), consultationRequest);
        assertThat(testRequest.getRequestId(), equalTo(updatedRequest.getRequestId()));
        assertThat(updatedRequest.getStatus(), equalTo(RequestStatus.COMPLETED));
        assertThat(updatedRequest.getConsultation().getSuggestion(), equalTo(consultationRequest.getSuggestion()));

    }


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        //Implement this method

        //Create an object of CreateConsultationRequest and call getCreateConsultationRequest() to create the object. Pass the above created object as the parameter

        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateConsultation() method
        // of consultationController with a negative long value as Id and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method


        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"

        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, ()->{
            consultationController.updateConsultation(-1L, consultationRequest);
        });

        assertThat(ex.getMessage(), containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        //Implement this method

        //Create an object of CreateConsultationRequest and call getCreateConsultationRequest() to create the object. Pass the above created object as the parameter
        // Set the suggestion of the above created object to null.

        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateConsultation() method
        // of consultationController with request Id of the testRequest object and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method

        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequest);
        /**
         * Once again status is set internally, not via controller. Assuming status here means suggestion
         */
        consultationRequest.setSuggestion(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, ()->{
            consultationController.updateConsultation(testRequest.getRequestId(), consultationRequest);
        });

        assertThat(ex.getMessage(), containsString("ConstraintViolationException"));


    }

    public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {

        //Create an object of CreateLabResult and set all the values
        // if the lab result test status is Positive, set the doctor suggestion as "HOME_QUARANTINE" and comments accordingly
        // else if the lab result status is Negative, set the doctor suggestion as "NO_ISSUES" and comments as "Ok"
        // Return the object

        CreateLabResult labResult = new CreateLabResult();
        labResult.setBloodPressure(testRequest.getLabResult().getBloodPressure());
        labResult.setHeartBeat(testRequest.getLabResult().getHeartBeat());
        labResult.setTemperature(testRequest.getLabResult().getTemperature());
        labResult.setOxygenLevel(testRequest.getLabResult().getOxygenLevel());
        labResult.setComments(testRequest.getLabResult().getComments());
        labResult.setResult(testRequest.getLabResult().getResult());

        CreateConsultationRequest request = new CreateConsultationRequest();
        if(labResult.getResult().equals(TestStatus.POSITIVE)){
            request.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
            request.setComments("Remain in home and avoid contact with anybody. Maintain minimum distance of 2 feet. Take medicines regularly");
        }else if(labResult.getResult().equals(TestStatus.NEGATIVE)){
            request.setSuggestion(DoctorSuggestion.NO_ISSUES);
            request.setComments("Ok");
        }

        return request; // Replace this line with your code

    }

}