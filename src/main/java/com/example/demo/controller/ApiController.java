package com.example.demo.controller;


import com.example.demo.exception.AppException;
import com.example.demo.exception.EnrollmentNotFoundException;
import com.example.demo.exception.ManagedBlockchainServiceException;
import com.example.demo.model.AMBConfig;
import com.example.demo.model.InvokeRequest;
import com.example.demo.service.ManagedBlockchainService;

import lombok.extern.slf4j.Slf4j;

import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;




@RestController
@Slf4j
public class ApiController {

    @Autowired
    ManagedBlockchainService service;

    ApiController() {
        log.debug("ApiController created....");
    }

    /**
     * Enroll a new Fabric user
     *
     * @return
     */
    @RequestMapping(path = "/enroll-lambda-user", method = RequestMethod.POST)
    public ResponseEntity<?> enrollUser() {
        try {
            log.debug("Enrolling user - user:" + AMBConfig.LAMBDAUSER);

            // Register and enroll user to Fabric CA
            service.setupClient();
            service.enrollUser(AMBConfig.LAMBDAUSER, AMBConfig.LAMBDAUSERPWD);

            return new ResponseEntity<>(AMBConfig.LAMBDAUSER + " enrolled successfully", HttpStatus.OK);
        } catch (AppException e) {
            log.error("Error while enrolling user - userId:" + AMBConfig.LAMBDAUSER);
            return new ResponseEntity<>("Error while enrolling user - " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ManagedBlockchainServiceException e) {
            log.error("Error while enrolling user, ManagedBlockchainService startup failed - " + e.getMessage());
            return new ResponseEntity<>("Error while enrolling user, ManagedBlockchainService startup failed - "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error while enrolling user - userId:" + AMBConfig.LAMBDAUSER);
            e.printStackTrace();
            return new ResponseEntity<>("Error while enrolling user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generic endpoint to query any function on any chaincode.
     *
     * @param chaincodeName Name of the chaincode
     * @param functionName Name of the function to query
     * @param args (optional) argument for the function to query
     * @return
     */
    @RequestMapping(path = "/query", method = RequestMethod.GET)
    public ResponseEntity<?> query(@RequestParam String chaincodeName,
                                   @RequestParam String functionName,
                                   @RequestParam(required = false) String args) {
        try {

            if (args == null)
                args = "";

            log.debug("Querying chaincode - chaincodeName:" + chaincodeName +
                    "functionName:" + functionName +
                    "args:" + args);

            service.setupClient();

            log.debug("Finished SetupClient.");
            // First retrieve LambdaUser's credentials and set user context
            service.setUser(AMBConfig.LAMBDAUSER);

            log.debug("Finished setUser.");

            service.initChannel();

            log.debug("Finished initChannel.");

            String res = service.queryChaincode(service.getClient(), service.getChannel(), chaincodeName, functionName, args);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (EnrollmentNotFoundException | AppException e){
            log.error("Error while querying chaincode - " + e.getMessage());
            return new ResponseEntity<>("Error while querying chaincode - " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ManagedBlockchainServiceException e) {
            log.error("Error while querying chaincode, " + e.getMessage());
            return new ResponseEntity<>("Error while querying chaincode, ManagedBlockchainService startup failed - "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ProposalException e) {
            log.error("Error while querying chaincode, " + e.getMessage());
            return new ResponseEntity<>("Error while querying chaincode, Proposal failed - "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error while querying - function:" + functionName + " chaincode:" + chaincodeName);
            e.printStackTrace();
            return new ResponseEntity<>("Error while querying chaincode", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generic endpoint to invoke any function on any chaincode
     *
     * @param invokeRequest InvokeRequest object containing:
     *                      - chaincodeName: name of the chaincode
     *                      - functionName: function to invoke
     *                      - argsList (optional): list of arguments for the function to invoke
     * @return
     */
    @RequestMapping(path = "/invoke", method = RequestMethod.POST)
    public ResponseEntity<?> invoke(@RequestBody @Valid InvokeRequest invokeRequest) {
        try {
            log.debug("Invoking chaincode with payload:" + invokeRequest.toString());

            service.setupClient();
            // First retrieve LambdaUser's credentials and set user context
            service.setUser(AMBConfig.LAMBDAUSER);
            service.initChannel();

            log.debug("Initialized Channel....");

            // build arguments list required by the chaincode
            String[] arguments = invokeRequest.getArgList().stream().toArray(String[]::new);

            service.invokeChaincode(service.getClient(), service.getChannel(),
                    invokeRequest.getChaincodeName(),
                    invokeRequest.getFunctionName(),
                    arguments);

            return new ResponseEntity<>("Invoke successful", HttpStatus.ACCEPTED);
        } catch (EnrollmentNotFoundException | AppException e){
            log.error("Error while invoking chaincode - " + e.getMessage());
            return new ResponseEntity<>("Error while invoking chaincode - " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ManagedBlockchainServiceException e) {
            log.error("Error while invoking chaincode, ManagedBlockchainService startup failed - " + e.getMessage());
            return new ResponseEntity<>("Error while invoking chaincode, ManagedBlockchainService startup failed - "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error while invoking - function:" + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("Error while invoking chaincode", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
