package com.bridgelabs.employeepayrollservicejdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class EmployeePayrollDBService {
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

	private Connection getConnection() throws EmployeePayrollException {
		String JDBC_URL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String USERNAME = "root";
		String PASSWORD = "abcd1234";
		Connection connection;
		try {
			connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
			logger.info("Connection is established");
			return connection;
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to establish connection to database");
		}
	}

	public List<EmployeePayrollData> readData() throws EmployeePayrollException {
		String sql = "select * from payroll_data";
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			return this.getEmployeePayrollListFromResultset(resultSet);
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to read from database");
		}
	}

	private List<EmployeePayrollData> getEmployeePayrollListFromResultset(ResultSet resultSet)
			throws EmployeePayrollException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<EmployeePayrollData>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String objectname = resultSet.getString("name");
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("salary");
				LocalDate start = resultSet.getDate("start").toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, objectname, salary, gender, start));
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
		String sql = String.format("update payroll_data set salary=%.2f where name='%s'", salary, name);
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
			String sql = "update payroll_data set salary=? where name=?";
			this.preparedStatementForUpdate = connection.prepareStatement(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to prepare statement");
		}
	}

	private void prepareStatementForEmployeePayrollDataRetrieval() throws EmployeePayrollException {
		try {
			Connection connection = this.getConnection();
			String sql = "select * from payroll_data where name=?";
			this.employeePayrollDataStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to prepare statement");
		}
	}

	public List<EmployeePayrollData> getEmployeePayrollDataByStartDate(LocalDate startDate, LocalDate endDate)
			throws EmployeePayrollException {
		String sql = String.format(
				"select * from payroll_data where start between cast('%s' as date) and cast('%s' as date);",
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
		String sql = String.format("select gender, %s(salary) as result from payroll_data group by gender",
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

	public EmployeePayrollData addEmployeeToDataBase(String name, String gender, double salary, LocalDate start)
			throws EmployeePayrollException {
		double basic_pay = salary;
		double dedeuctions = 0.2 * basic_pay;
		double taxable_pay = basic_pay - dedeuctions;
		double tax = 0.1 * taxable_pay;
		double net_pay = basic_pay - tax;
		int employeeId = 0;
		EmployeePayrollData employeePayrollData = null;
		Connection connection = this.getConnection();
		try {
			connection.setAutoCommit(false);
			String sql1 = String.format(
					"insert into payroll_data (name,gender,salary,start) values ('%s','%s',%.2f,'%s')", name, gender,
					salary, start.toString());
			Statement statement1 = connection.createStatement();
			int rowsAffected = statement1.executeUpdate(sql1, statement1.RETURN_GENERATED_KEYS);
			if (rowsAffected == 1) {
				ResultSet resultSet = statement1.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			} else {
				throw new EmployeePayrollException("Unable to add employee to payroll_data");
			}

			String sql2 = String.format("insert into payroll_details values (%s,%s,%s,%s,%s,%s)", employeeId, basic_pay,
					dedeuctions, taxable_pay, tax, net_pay);
			Statement statement2 = connection.createStatement();
			rowsAffected = statement2.executeUpdate(sql2);
			if (rowsAffected == 1) {
				employeePayrollData = new EmployeePayrollData(employeeId, name, salary, gender);
			} else {
				throw new EmployeePayrollException("Unable to add employee to payroll_data");
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
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
