package com.bridgelabs.employeepayrollservicejdbc;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EmployeePayrollServiceTest {
	public EmployeePayrollService employeePayrollService;
	private static final Logger logger=LogManager.getFormatterLogger(EmployeePayrollServiceTest.class);
	
	@Before
	public void setUp() {
		this.employeePayrollService=new EmployeePayrollService();
	}

	@Test
	public void payrollDataWhenReadFromDatabaseShouldReturnCorrectList() {
		List<EmployeePayrollData> employeePayrollList;
		try {
			employeePayrollList = this.employeePayrollService.getEmployeePayrollData();
			Assert.assertEquals(3,employeePayrollList.size());
		} catch (EmployeePayrollException e) {
			logger.info(e.getMessage());
		}
	}
	
	@Test
	public void givenEmployeePayrollDataWhenUpdatedShouldMatchWithDB() {
		try {
			this.employeePayrollService.getEmployeePayrollData();
			String name="Terissa";
			double salary= 3000000.00;
			boolean result= this.employeePayrollService.updateEmployeeData(name,salary);
			Assert.assertTrue(result);
			Assert.assertTrue(this.employeePayrollService.checkIfEmployeePayrollListInSyncWithDb(name));
		} catch (EmployeePayrollException e) {
			logger.info(e.getMessage());
		}
		
	}
}
