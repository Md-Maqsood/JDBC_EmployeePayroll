package com.bridgelabs.employeepayrollservicejdbc;

import java.util.List;

public class EmployeePayrollService {
	private EmployeePayrollDBService employeePayrollDBService;
	private List<EmployeePayrollData> employeePayrollList;
	
	public EmployeePayrollService() {
		this.employeePayrollDBService = EmployeePayrollDBService.getInstance();
	}
	
	public List<EmployeePayrollData> getEmployeePayrollData() throws EmployeePayrollException{
		this.employeePayrollList = this.employeePayrollDBService.readData();
		return this.employeePayrollList;
	}
	
	public EmployeePayrollData getEmployeePayrollDataFromList(String name){
		return this.employeePayrollList.stream()
				.filter(employeePayrollObject->employeePayrollObject.getName().equals(name))
				.findFirst().orElse(null);
	}
	
	public boolean checkIfEmployeePayrollListInSyncWithDb(String name) throws EmployeePayrollException {
		List<EmployeePayrollData> employeePayrollDatas=this.employeePayrollDBService.getEmployeePayrollDataFromDB(name);
		return employeePayrollDatas.get(0).equals(getEmployeePayrollDataFromList(name));
	}
	
	public boolean updateEmployeeData(String name, double salary) throws EmployeePayrollException {
		int rowsAffected=this.employeePayrollDBService.updateEmployeeData(name, salary);
		if(rowsAffected==0)	return false;
		EmployeePayrollData employeePayrollObject=this.getEmployeePayrollDataFromList(name);
		if(employeePayrollObject!=null) {
			employeePayrollObject.setSalary(salary);
			return true;
		}
		return false;
	}
}
