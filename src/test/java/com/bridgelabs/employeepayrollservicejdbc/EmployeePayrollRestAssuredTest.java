package com.bridgelabs.employeepayrollservicejdbc;

import java.time.LocalDate;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class EmployeePayrollRestAssuredTest {
	private static Logger logger = LogManager.getLogger(EmployeePayrollRestAssuredTest.class);
	private int empId;

	@Before
	public void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
		empId = 9;
	}

	@Test
	public void givenNewEmployee_WhenAdded_ShouldMatch201ResponseandCount() throws EmployeePayrollException {
		EmployeePayrollService employeePayrollService=new EmployeePayrollService(Arrays.asList(getEmployeeList()));
		EmployeePayrollData employeePayrollData=new EmployeePayrollData(0, "Mark", 150000, "M", LocalDate.now());
		Response response=addEmployeeToJsonServer(employeePayrollData);
		int statusCode=response.getStatusCode();
		Assert.assertEquals(201, statusCode);
		employeePayrollData=new Gson().fromJson(response.asString(),EmployeePayrollData.class);
		employeePayrollService.addEmployeeToPayrollUsingRestIo(employeePayrollData);
		int entries=employeePayrollService.countEntries();
		Assert.assertEquals(4, entries);
	}
	private Response addEmployeeToJsonServer(EmployeePayrollData employeePayrollData) {
		String empJson=new Gson().toJson(employeePayrollData,EmployeePayrollData.class);
		RequestSpecification request=RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employees");
	}

	private EmployeePayrollData[] getEmployeeList() {
		Response response=RestAssured.get("/employees");
		logger.info("Employees in json server: "+response.asString());
		EmployeePayrollData[] arrayOfEmps=new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrayOfEmps;
	}
}
