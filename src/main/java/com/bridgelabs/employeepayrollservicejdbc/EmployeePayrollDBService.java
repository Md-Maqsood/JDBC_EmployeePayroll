package com.bridgelabs.employeepayrollservicejdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.cj.result.SqlDateValueFactory;

@SuppressWarnings("unused")
public class EmployeePayrollDBService {
	public int connectionCounter=0;
	private static final Logger logger = LogManager.getLogger(EmployeePayrollDBService.class);
	private static EmployeePayrollDBService employeePayrollDBService;
	private PreparedStatement preparedStatementForUpdate;
	private PreparedStatement employeePayrollDataStatement;

	private EmployeePayrollDBService() {
	}

	public static EmployeePayrollDBService getInstance() {
		if (employeePayrollDBService == null) {
			employeePayrollDBService = new EmployeePayrollDBService();
		}
		return employeePayrollDBService;
	}

	private synchronized Connection getConnection() throws EmployeePayrollException {
		connectionCounter++;
		String JDBC_URL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String USERNAME = "root";
		String PASSWORD = "abcd1234";
		Connection connection;
		try {
			logger.info("Processing thread: "+Thread.currentThread().getName()+"Connecting to database with Id: "+connectionCounter);
			connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
			logger.info("Processing thread :"+Thread.currentThread().getName()+"Id: "+connectionCounter+" Connection is established");
			return connection;
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to establish connection to database");
		}
	}

	public List<EmployeePayrollData> readData() throws EmployeePayrollException {
		String sql = "select payroll_data.employee_id,name, gender, net_pay, start, address, phone_number, company_name from payroll_data join company on payroll_data.company_id=company.company_id join payroll_details on payroll_data.employee_id=payroll_details.employee_id where is_active;";
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			return this.getEmployeePayrollListFromResultset(resultSet);
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to read from database");
		}
	}

	private List<String> getDepartments(int employeeId) throws SQLException, EmployeePayrollException {
		String sql = String.format(
				"select department_name from payroll_data join employee_department on payroll_data.employee_id=employee_department.employee_id join department on employee_department.department_id=department.department_id where payroll_data.employee_id=%s;",
				employeeId);
		List<String> departments = new ArrayList<String>();
		Connection connection = this.getConnection();
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				departments.add(resultSet.getString("department_name"));
			}
			return departments;
		} finally {
			connection.close();
		}
	}

	private List<EmployeePayrollData> getEmployeePayrollListFromResultset(ResultSet resultSet)
			throws EmployeePayrollException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<EmployeePayrollData>();
		try {
			while (resultSet.next()) {
				int employeeId = resultSet.getInt("employee_id");
				String objectname = resultSet.getString("name");
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("net_pay");
				LocalDate start = resultSet.getDate("start").toLocalDate();
				String address = resultSet.getString("address");
				String phone_number = resultSet.getString("phone_number");
				String company = resultSet.getString("company_name");
				List<String> departments = this.getDepartments(employeeId);
				employeePayrollList.add(new EmployeePayrollData(employeeId, objectname, salary, gender, company,
						address, phone_number, start, departments));
			}
			return employeePayrollList;
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to use the result set");
		}
	}

	public List<EmployeePayrollData> getEmployeePayrollDataFromDB(String name) throws EmployeePayrollException {
		if (this.employeePayrollDataStatement == null) {
			this.prepareStatementForEmployeePayrollDataRetrieval();
		}
		try (Connection connection = this.getConnection()) {
			this.employeePayrollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			return this.getEmployeePayrollListFromResultset(resultSet);
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to read from database");
		}
	}

	public int updateEmployeeData(String name, double salary) throws EmployeePayrollException {
		return this.updateEmployeePayrollDataUsingPrepredStatement(name, salary);
	}

	private int updateEmployeePayrollDataUsingStatement(String name, double salary) throws EmployeePayrollException {
		String sql = String.format(
				"update payroll_details set net_pay=%.2f where employee_id=(select employee_id from payroll_data where name='%s' and is_active);",
				salary, name);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			int rowsAffected = statement.executeUpdate(sql);
			return rowsAffected;
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable To update data in database");
		}
	}

	private int updateEmployeePayrollDataUsingPrepredStatement(String name, double salary)
			throws EmployeePayrollException {
		if (this.preparedStatementForUpdate == null) {
			this.prepareStatementForEmployeePayrollUpdate();
		}
		try {
			preparedStatementForUpdate.setDouble(1, salary);
			preparedStatementForUpdate.setString(2, name);
			int rowsAffected = preparedStatementForUpdate.executeUpdate();
			return rowsAffected;
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to use prepared statement");
		}
	}

	private void prepareStatementForEmployeePayrollUpdate() throws EmployeePayrollException {
		try {
			Connection connection = this.getConnection();
			String sql = "update payroll_details set net_pay=? where employee_id=(select employee_id from payroll_data where name=? and is_active);";
			this.preparedStatementForUpdate = connection.prepareStatement(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to prepare statement");
		}
	}

	private void prepareStatementForEmployeePayrollDataRetrieval() throws EmployeePayrollException {
		try {
			Connection connection = this.getConnection();
			String sql = "select payroll_data.employee_id,name, gender, net_pay, start, address, phone_number, company_name from payroll_data join company on payroll_data.company_id=company.company_id join payroll_details on payroll_data.employee_id=payroll_details.employee_id where name=? and is_active;";
			this.employeePayrollDataStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to prepare statement");
		}
	}

	public List<EmployeePayrollData> getEmployeePayrollDataByStartDate(LocalDate startDate, LocalDate endDate)
			throws EmployeePayrollException {
		String sql = String.format(
				"select payroll_data.employee_id,name, gender, net_pay, start, address, phone_number, company_name from payroll_data join company on payroll_data.company_id=company.company_id join payroll_details on payroll_data.employee_id=payroll_details.employee_id where start between cast('%s' as date) and cast('%s' as date) and is_active;",
				startDate.toString(), endDate.toString());
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			return this.getEmployeePayrollListFromResultset(resultSet);
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to get connection");
		}
	}

	public ComputationResult makeComputations(ComputationType computationType) throws EmployeePayrollException {
		String sql = String.format(
				"select gender, %s(net_pay) as result from payroll_data join payroll_details on payroll_data.employee_id=payroll_details.employee_id where is_active group by gender",
				computationType.toString());
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			double maleComputationResult = 0.0;
			double femaleComputationResult = 0.0;
			while (resultSet.next()) {
				if (resultSet.getString("gender").equals("M"))
					maleComputationResult = resultSet.getDouble("result");
				else
					femaleComputationResult = resultSet.getDouble("result");
			}
			return new ComputationResult(computationType, femaleComputationResult, maleComputationResult);
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to use resultset");
		}
	}

	private boolean addDepartmentToDataBase(int employeeId, String department, Connection connection) {
		String sql = String.format("select department_id from department where department_name='%s'", department);
		int departmentId = 0;
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.next()) {
				departmentId = resultSet.getInt("department_id");
			} else {
				sql = String.format("insert into department (department_name) values ('%s')", department);
				int rowsAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
				if (rowsAffected == 1) {
					resultSet = statement.getGeneratedKeys();
					if (resultSet.next())
						departmentId = resultSet.getInt(1);
				} else {
					return false;
				}
			}
			sql = String.format("insert into employee_department (employee_id, department_id) values (%s,%s)", employeeId,
					departmentId);
			int rowsAffected = statement.executeUpdate(sql);
			if (rowsAffected != 1) {
				return false;
			}
			return true;
		}catch(SQLException e){
			return false;
		}
	}

	public void makeEmployeeInactiveInDataBase(String employeeName) throws EmployeePayrollException {
		String sql = String.format("update payroll_data set is_active=0 where name='%s' and is_active", employeeName);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			int rowsAffected = statement.executeUpdate(sql);
			if (rowsAffected == 0)
				throw new EmployeePayrollException("Unable to delete employee or employee not found");
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to delete employee from database");
		}
	}
	
	private int addCompanyDetailsReturnCompanyId(Connection connection, String company){
		int companyId = -1;
		try{
			Statement statement0 = connection.createStatement();
			ResultSet resultSet = statement0
					.executeQuery(String.format("select company_id from company where company_name='%s'", company));
			if (resultSet.next()) {
				companyId = resultSet.getInt("company_id");
			} else {
				String sql0 = String.format("insert into company (company_name) values ('%s')", company);
				Statement statement00 = connection.createStatement();
				int rowsAffected = statement00.executeUpdate(sql0, statement00.RETURN_GENERATED_KEYS);
				if (rowsAffected == 1) {
					resultSet = statement00.getGeneratedKeys();
					if (resultSet.next()) companyId = resultSet.getInt(1);
				}
			}
			return companyId;
		}catch(SQLException e) {
			return -1;
		}
	}
	
	private int addEmployeeDetailsToPayrollDataReturnEmployeeId(Connection connection, String name, String gender, LocalDate start, int companyId, String address, String phoneNumber) {
		int employeeId=-1;
		String sql1 = String.format(
				"insert into payroll_data (name,gender,start,company_id, address, phone_number) values ('%s','%s','%s',%s,'%s','%s')",
				name, gender, start.toString(), companyId, address, phoneNumber);
		try{
			Statement statement1 = connection.createStatement();
			int rowsAffected = statement1.executeUpdate(sql1, statement1.RETURN_GENERATED_KEYS);
			if (rowsAffected == 1) {
				ResultSet resultSet = statement1.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
			return employeeId;
		}catch(SQLException e) { 
			return -1;
		}		
	}

	public synchronized EmployeePayrollData addEmployeeToDataBase(String company, String address, String phoneNumber, String name,
			String gender, double salary, LocalDate start, List<String> departments) throws EmployeePayrollException {
		double basic_pay = salary;
		double dedeuctions = 0.2 * basic_pay;
		double taxable_pay = basic_pay - dedeuctions;
		double tax = 0.1 * taxable_pay;
		double net_pay = basic_pay - tax;
		List<Integer> employeeId = new ArrayList<Integer>();
		employeeId.add(-1);
		List<Integer> companyId = new ArrayList<Integer>();
		companyId.add(-1);
		Map<String, Boolean> whetherDepartmentIsAdded=new HashMap<String, Boolean>();
		departments.forEach(department->whetherDepartmentIsAdded.put(department, false));
		Map<String,Boolean> threadsExecutionStatus=new HashMap<String, Boolean>();
		EmployeePayrollData employeePayrollData = null;
		Connection connection = this.getConnection();
		try {
			connection.setAutoCommit(false);
			Runnable taskCompany=()->{
				threadsExecutionStatus.put(Thread.currentThread().getName(), false);
				logger.info("Adding to company with thread: "+Thread.currentThread().getName());
				companyId.set(0,this.addCompanyDetailsReturnCompanyId(connection, company));
				logger.info("Added to company with thread: "+Thread.currentThread().getName());
				threadsExecutionStatus.put(Thread.currentThread().getName(), true);
			};			
			Runnable taskPayrollData=()->{
				threadsExecutionStatus.put(Thread.currentThread().getName(), false);
				logger.info("Adding to payroll_data with thread: "+Thread.currentThread().getName());
				employeeId.set(0,this.addEmployeeDetailsToPayrollDataReturnEmployeeId(connection, name, gender, start, companyId.get(0), address, phoneNumber));
				logger.info("Added to payroll_data with thread: "+Thread.currentThread().getName());
				threadsExecutionStatus.put(Thread.currentThread().getName(), true);
			};
			Runnable taskDepartment=()->{
				threadsExecutionStatus.put(Thread.currentThread().getName(), false);
				logger.info("Adding to department with thread: "+Thread.currentThread().getName());
				for (String department : departments) {
					whetherDepartmentIsAdded.put(department,this.addDepartmentToDataBase(employeeId.get(0), department, connection));
				}
				logger.info("Adding to department with thread: "+Thread.currentThread().getName());
				threadsExecutionStatus.put(Thread.currentThread().getName(), true);
			};
			Thread thread1=new Thread(taskCompany, "company");
			Thread thread2=new Thread(taskPayrollData, "payrollData");
			Thread thread3=new Thread(taskDepartment, "department");
			thread1.start();
			try {
				thread1.join();
			} catch (InterruptedException e) {
				throw new EmployeePayrollException("Unable to add Company");
			}
			if(companyId.get(0)==-1) throw new EmployeePayrollException("Unable to add Company");
			thread2.start();
			try {
				thread2.join();
			} catch (InterruptedException e) {
				throw new EmployeePayrollException("Unable to add to payroll_data");
			}
			if(employeeId.get(0)==-1) throw new EmployeePayrollException("Unable to add to payroll_data");
			thread3.start();
			while(threadsExecutionStatus.containsValue(false)) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					throw new EmployeePayrollException("Unable to add employee to payroll_data table");
				}
			}
			String sql2 = String.format("insert into payroll_details values (%s,%s,%s,%s,%s,%s)", employeeId.get(0), basic_pay,
					dedeuctions, taxable_pay, tax, net_pay);
			Statement statement2 = connection.createStatement();
			int rowsAffected = statement2.executeUpdate(sql2);
			if (rowsAffected == 1) {
				employeePayrollData = new EmployeePayrollData(employeeId.get(0), name, net_pay, gender, company, address,
						phoneNumber, start, departments);
			} else {
				throw new EmployeePayrollException("Unable to add employee to payroll_details");
			}
			connection.commit();
			return employeePayrollData;
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new EmployeePayrollException("Unable to add employee to payroll_data table");
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
