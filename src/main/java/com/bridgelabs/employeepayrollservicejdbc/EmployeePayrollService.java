package com.bridgelabs.employeepayrollservicejdbc;

import java.util.List;

public class EmployeePayrollService {
	private EmployeePayrollDBService employeePayrollDBService;
	
	public EmployeePayrollService() {
		this.employeePayrollDBService = new EmployeePayrollDBService();
	}
	
	public List<EmployeePayrollData> getEmployeePayrollData() throws EmployeePayrollException{
		return this.employeePayrollDBService.readData();
	}
}
