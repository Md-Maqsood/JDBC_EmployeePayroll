package com.bridgelabs.employeepayrollservicejdbc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class EmployeePayrollRestAssuredTest {
	private static Logger logger = LogManager.getLogger(EmployeePayrollRestAssuredTest.class);
	
	@Before
	public void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	@Ignore
	public void givenNewEmployee_WhenAdded_ShouldMatch201ResponseandCount(){
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(Arrays.asList(getEmployeeList()));
		EmployeePayrollData employeePayrollData = new EmployeePayrollData(0, "Mark", 150000, "M", LocalDate.now());
		Response response = addEmployeeToJsonServer(employeePayrollData);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(201, statusCode);
		employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
		employeePayrollService.addEmployeeToPayrollUsingRestIo(employeePayrollData);
		int entries=-1;
		try {
			entries = employeePayrollService.countEntries();
		} catch (EmployeePayrollException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(4, entries);
	}

	@Ignore
	public void given6Employees_WhenAddedShouldMatchCount(){
		EmployeePayrollService employeePayrollService=new EmployeePayrollService(Arrays.asList(getEmployeeList()));
		EmployeePayrollData[] employeePayrollDataList= {
				new EmployeePayrollData(0, "Jeff Bezos", 100000.0, "M",LocalDate.now()),
				new EmployeePayrollData(0, "Bill Gates", 200000.0, "M",LocalDate.now()),
				new EmployeePayrollData(0, "Mark Zuckerberg", 300000.0, "M", LocalDate.now()),
				new EmployeePayrollData(0, "Sunder", 600000.0, "M",LocalDate.now()),
				new EmployeePayrollData(0, "Mukesh", 100000.0, "M",LocalDate.now()),
				new EmployeePayrollData(0, "Anil", 200000.0, "M",LocalDate.now())
		};
		addMultipleEmployees(employeePayrollService,Arrays.asList(employeePayrollDataList));
		int entries=-1;
		try {
			entries = employeePayrollService.countEntries();
		} catch (EmployeePayrollException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(10, entries);
	}
	
	@Test
	public void givenEmployeeDeatails_WhenUpdatedShouldMatchNewSalary() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(Arrays.asList(getEmployeeList()));
		EmployeePayrollData employeePayrollData = new EmployeePayrollData(2, "Bill Gates", 250000, "M", LocalDate.now());
		this.updateEmployeeDetailsToJsonServer(employeePayrollData, employeePayrollService);
		double updatedSalary=Arrays.asList(getEmployeeList()).stream()
				.filter(employee->employee.getName().equals("Bill Gates"))
				.findFirst().orElse(null).getSalary();
		Assert.assertEquals(250000.0, updatedSalary,0.0);		
	}
	
	@Ignore
	public void givenEmployeeDetailsOnJsonServer_WhenRetrievedShouldMatchCount() {
		EmployeePayrollData[] arrayOfEmps=this.getEmployeeList();
		EmployeePayrollService employeePayrollService=new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		int numOfEmployees=-1;
		try {
			numOfEmployees = employeePayrollService.countEntries();
		} catch (EmployeePayrollException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(3,numOfEmployees);
	}
	
	@Ignore
	public void givenEmployeeDataOnJsonServer_WhenDeleted_ShouldMatch200StatusCodeAndCount() {
		EmployeePayrollData[] arrayOfEmps=this.getEmployeeList();
		EmployeePayrollService employeePayrollService=new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		int employeeIdToBeDeleted=2;
		Response response=this.deleteEmployee(employeeIdToBeDeleted);
		int statusCode=response.getStatusCode();
		Assert.assertEquals(200, statusCode);
		int entriesAfterDeletion=-1;
		try {
			employeePayrollService.deleteEmployeeFromPayrollWithRestIO(employeeIdToBeDeleted);
			entriesAfterDeletion=employeePayrollService.countEntries();
		} catch (EmployeePayrollException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(2, entriesAfterDeletion);
	}

	private Response deleteEmployee(int employeeIdToBeDeleted) {
		RequestSpecification request=RestAssured.given();
		request.header("Content-Type", "application/json");
		return request.delete("/employees/"+employeeIdToBeDeleted);
	}

	private void updateEmployeeDetailsToJsonServer(EmployeePayrollData employeePayrollData,EmployeePayrollService employeePayrollService) {
		String empJson = new Gson().toJson(employeePayrollData, EmployeePayrollData.class);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		Response response=request.put("/employees/2");
		if(response.getStatusCode()==200) {
			try {
				employeePayrollService.updateEmployeeToPayrollUsingRestIo(employeePayrollData);
			} catch (EmployeePayrollException e) {
				e.printStackTrace();
			}
		}
	}

	private void addMultipleEmployees(EmployeePayrollService employeePayrollService, List<EmployeePayrollData> employeePayrollDataList) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<Integer, Boolean>();
		employeePayrollDataList.forEach(employeePayrollData->employeeAdditionStatus.put(employeePayrollData.hashCode(), false));
		for (EmployeePayrollData employeePayrollData : employeePayrollDataList) {
			Runnable task = () -> {
				Response response=this.addEmployeeToJsonServer(employeePayrollData);
				if(response.getStatusCode()==201) {
					employeePayrollService.addEmployeeToPayrollUsingRestIo(employeePayrollData);
				}
				employeeAdditionStatus.put(employeePayrollData.hashCode(), true);
			};
			Thread thread = new Thread(task, employeePayrollData.getName());
			thread.start();
		}
		while (employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized Response addEmployeeToJsonServer(EmployeePayrollData employeePayrollData) {
		String empJson = new Gson().toJson(employeePayrollData, EmployeePayrollData.class);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employees");
	}

	private EmployeePayrollData[] getEmployeeList() {
		Response response = RestAssured.get("/employees");
		logger.info("Employees in json server: " + response.asString());
		EmployeePayrollData[] arrayOfEmps = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrayOfEmps;
	}
}
