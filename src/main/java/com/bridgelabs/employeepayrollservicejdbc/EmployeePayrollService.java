package com.bridgelabs.employeepayrollservicejdbc;

import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmployeePayrollService {
	private static final Logger logger=LogManager.getFormatterLogger(EmployeePayrollService.class);
	private EmployeePayrollDBService employeePayrollDBService;
	private List<EmployeePayrollData> employeePayrollList;

	public EmployeePayrollService() {
		this.employeePayrollDBService = EmployeePayrollDBService.getInstance();
	}

	public List<EmployeePayrollData> getEmployeePayrollData() throws EmployeePayrollException {
		this.employeePayrollList = this.employeePayrollDBService.readData();
		return this.employeePayrollList;
	}
	
	public int countEntries() throws EmployeePayrollException {
		return this.getEmployeePayrollData().size();
	}

	public EmployeePayrollData getEmployeePayrollDataFromList(String name) {
		return this.employeePayrollList.stream()
				.filter(employeePayrollObject -> employeePayrollObject.getName().equals(name)).findFirst().orElse(null);
	}

	public boolean checkIfEmployeePayrollListInSyncWithDb(String name) throws EmployeePayrollException {
		List<EmployeePayrollData> employeePayrollDatas = this.employeePayrollDBService
				.getEmployeePayrollDataFromDB(name);
		EmployeePayrollData employeePayrollData = getEmployeePayrollDataFromList(name);
		if (employeePayrollDatas.isEmpty()) {
			if (employeePayrollData == null)
				return true;
			else
				return false;
		}
		return employeePayrollDatas.get(0).equals(employeePayrollData);
	}

	public boolean updateEmployeeData(String name, double salary) throws EmployeePayrollException {
		int rowsAffected = this.employeePayrollDBService.updateEmployeeData(name, salary);
		if (rowsAffected == 0)
			return false;
		EmployeePayrollData employeePayrollObject = this.getEmployeePayrollDataFromList(name);
		if (employeePayrollObject != null) {
			employeePayrollObject.setSalary(salary);
			return true;
		}
		return false;
	}

	public List<EmployeePayrollData> getEmployeePayrollDataByStartDate(LocalDate startDate, LocalDate endDate)
			throws EmployeePayrollException {
		return this.employeePayrollDBService.getEmployeePayrollDataByStartDate(startDate, endDate);
	}

	public ComputationResult makeComputations(ComputationType computationType) throws EmployeePayrollException {
		return this.employeePayrollDBService.makeComputations(computationType);
	}

	public void addEmployeeToDatabase(String company, String address, String phone_number, String name, String gender,
			double salary, LocalDate start, List<String> departments) throws EmployeePayrollException {
		EmployeePayrollData employeePayrollData = this.employeePayrollDBService.addEmployeeToDataBase(company, address,
				phone_number, name, gender, salary, start, departments);
		if (employeePayrollData == null) {
			throw new EmployeePayrollException("Unable to add employee");
		}
		this.employeePayrollList.add(employeePayrollData);
	}

	public void deleteEmployee(String name) throws EmployeePayrollException {
		List<EmployeePayrollData> toBeDeleted = this.employeePayrollDBService.getEmployeePayrollDataFromDB(name);
		this.employeePayrollDBService.makeEmployeeInactiveInDataBase(name);
		toBeDeleted.forEach(employeePayrollData -> this.employeePayrollList.remove(employeePayrollData));
	}

	public void addEmployeesToDatabase(List<EmployeePayrollData> employeePayrollDatalist) throws EmployeePayrollException {
		for(EmployeePayrollData employeePayrollData: employeePayrollDatalist) {
			logger.info("Employee being added: "+employeePayrollData.getName());
			this.addEmployeeToDatabase(employeePayrollData.getCompany(), employeePayrollData.getAddress(), employeePayrollData.getPhone_number(), employeePayrollData.getName(), employeePayrollData.getGender(), employeePayrollData.getSalary(), employeePayrollData.getStart(), employeePayrollData.getDepartments());
			logger.info("Employee Added: "+employeePayrollData.getName());
		};
		logger.info(this.employeePayrollList);
	}
}
