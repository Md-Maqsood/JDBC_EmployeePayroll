package com.bridgelabs.employeepayrollservicejdbc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EmployeePayrollServiceTest {
	public EmployeePayrollService employeePayrollService;
	private static final Logger logger = LogManager.getFormatterLogger(EmployeePayrollServiceTest.class);

	@Before
	public void setUp() {
		this.employeePayrollService = new EmployeePayrollService();
	}

	@Test
	public void givenEmployeePayrollDataWhenUpdatedShouldMatchWithDB() {
		try {
			this.employeePayrollService.getEmployeePayrollData();
			String name = "Terisa";
			double salary = 3000000.00;
			boolean result = this.employeePayrollService.updateEmployeeData(name, salary);
			Assert.assertTrue(result);
			Assert.assertTrue(this.employeePayrollService.checkIfEmployeePayrollListInSyncWithDb(name));
		} catch (EmployeePayrollException e) {
			logger.info(e.getMessage());
		}

	}

	@Test
	public void givenEmployeePayrollDataWhenUpdatedUsingPreparedStatementShouldMatchWithDB() {
		try {
			this.employeePayrollService.getEmployeePayrollData();
			String name = "Terisa";
			double salary = 3000000.00;
			boolean result = this.employeePayrollService.updateEmployeeData(name, salary);
			Assert.assertTrue(result);
			Assert.assertTrue(this.employeePayrollService.checkIfEmployeePayrollListInSyncWithDb(name));
		} catch (EmployeePayrollException e) {
			logger.info(e.getMessage());
		}

	}

	@Test
	public void givenEmployeePayrollDataWhenMadeComputationsShouldReturnProperResults() {
		try {
			this.employeePayrollService.getEmployeePayrollData();
			ComputationResult result = this.employeePayrollService.makeComputations(ComputationType.AVG);
			Assert.assertEquals(7280000.0, result.maleResult, 0.0);
			Assert.assertEquals(3000000.0, result.femaleResult, 0.0);
		} catch (EmployeePayrollException e) {
			logger.info(e.getMessage());
		}
	}

	@Test
	public void givenEmployeeWhenAddedToDataBaseAndCheckedShouldBepresent() {
		try {
			this.employeePayrollService.getEmployeePayrollData();
			String name = "Mark";
			double salary = 2000000.0;
			String gender = "M";
			LocalDate start = LocalDate.parse("2018-01-31");
			String company = "Capgemini";
			String address = "xyz";
			String phone_number = "9123456789";
			List<String> departments = new ArrayList<String>();
			departments.add("Sales");
			departments.add("Logistics");
			this.employeePayrollService.addEmployeeToDatabase(company, address, phone_number, name, gender, salary,
					start, departments);
			Assert.assertTrue(this.employeePayrollService.checkIfEmployeePayrollListInSyncWithDb(name));
		} catch (EmployeePayrollException e) {
			logger.info(e.getMessage());
		}
	}

}
