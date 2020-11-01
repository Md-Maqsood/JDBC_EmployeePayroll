package com.bridgelabs.employeepayrollservicejdbc;

public class EmployeePayrollService {
	private EmployeePayrollDBService employeePayrollDBService;
	
	public EmployeePayrollService() {
		this.employeePayrollDBService = new EmployeePayrollDBService();
	}

	public boolean establishConnection() {
		return this.employeePayrollDBService.establishConnection();		
	}

}
