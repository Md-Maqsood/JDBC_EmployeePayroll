package com.bridgelabs.employeepayrollservicejdbc;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class EmployeePayrollMultiThreadingTest {
	private static final Logger logger=LogManager.getFormatterLogger(EmployeePayrollMultiThreadingTest.class);
	
	@Test
	public void given6Employees_WhenAddedToDB_ShouldMatchEmployeeEntries() {
		EmployeePayrollData[] arraysOfEmps= {
				new EmployeePayrollData(0, "Jeff Bezos", 100000.0, "M", "Capgemini", "abc", "4567665123", LocalDate.now(), Arrays.asList(new String[] {"Marketing"})),
				new EmployeePayrollData(0, "Bill Gates", 200000.0, "M", "Capgemini", "def", "4567665124", LocalDate.now(), Arrays.asList(new String[] {"Marketing"})),
				new EmployeePayrollData(0, "Mark Zuckerberg", 300000.0, "M", "Capgemini", "ghi", "4567865123", LocalDate.now(), Arrays.asList(new String[] {"Marketing"})),
				new EmployeePayrollData(0, "Sunder", 600000.0, "M", "Capgemini", "jkl", "4567665122", LocalDate.now(), Arrays.asList(new String[] {"Marketing"})),
				new EmployeePayrollData(0, "Mukesh", 100000.0, "M", "Capgemini", "mno", "4567665120", LocalDate.now(), Arrays.asList(new String[] {"Marketing"})),
				new EmployeePayrollData(0, "Anil", 200000.0, "M", "Capgemini", "pqr", "4567665127", LocalDate.now(), Arrays.asList(new String[] {"Marketing"}))
		};
		EmployeePayrollService employeePayrollService=new EmployeePayrollService();
		try {
			employeePayrollService.getEmployeePayrollData();
			Instant start=Instant.now();
			employeePayrollService.addEmployeesToDatabase(Arrays.asList(arraysOfEmps));
			Instant end=Instant.now();
			logger.info("Duration without thread: "+Duration.between(start, end));
			Assert.assertEquals(9,employeePayrollService.countEntries());
		} catch (EmployeePayrollException e) {
			e.printStackTrace();
		}
		
	}
}
