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
		String sql=String.format("select gender, %s(salary) from payroll_data group by gender",computationType.toString());
		try(Connection connection=this.getConnection()){
			Statement statement=connection.createStatement();
			ResultSet resultSet=statement.executeQuery(sql);
			double maleComputationResult=0.0;
			double femaleComputationResult=0.0;
			while(resultSet.next()) {
				if(resultSet.getString("gender").equals("M")) maleComputationResult=resultSet.getDouble("salary");
				else femaleComputationResult=resultSet.getDouble("salary");
			}
			return new ComputationResult(computationType, femaleComputationResult, maleComputationResult);
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to use resultset");
		}
	}

}
