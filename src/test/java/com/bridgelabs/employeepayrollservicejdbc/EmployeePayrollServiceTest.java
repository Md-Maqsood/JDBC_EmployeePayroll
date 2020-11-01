package com.bridgelabs.employeepayrollservicejdbc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EmployeePayrollServiceTest {
	public EmployeePayrollService employeePayrollService;
	
	@Before
	public void setUp() {
		this.employeePayrollService=new EmployeePayrollService();
	}

	@Test
	public void connectionWhenEstablishedShouldReturnTrue() {
		boolean result= this.employeePayrollService.establishConnection();
		Assert.assertTrue(result);
	}
}
