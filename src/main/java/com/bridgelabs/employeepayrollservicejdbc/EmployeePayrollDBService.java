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
	private static final Logger logger=LogManager.getLogger(EmployeePayrollDBService.class);
	private static EmployeePayrollDBService employeePayrollDBService;
	private PreparedStatement preparedStatement;
	
	private EmployeePayrollDBService() {}
	
	public static EmployeePayrollDBService getInstance() {
		if(employeePayrollDBService==null) {
			employeePayrollDBService=new EmployeePayrollDBService();
		}
		return employeePayrollDBService;
	}
	
	private Connection getConnection() throws EmployeePayrollException {
		String JDBC_URL="jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String USERNAME="root";
		String PASSWORD="abcd1234";
		Connection connection;
		try {
			connection=DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
			logger.info("Connection is established");
			return connection;
		}catch(SQLException e) {
			throw new EmployeePayrollException("Unable to establish connection to database");
		}
	}
	
	public List<EmployeePayrollData> readData() throws EmployeePayrollException{
		String sql="select * from payroll_data";
		List<EmployeePayrollData> employeePayrollList=new ArrayList<EmployeePayrollData>();
		try (Connection connection=this.getConnection()){
			Statement statement=connection.createStatement();
			ResultSet resultSet=statement.executeQuery(sql);
			while(resultSet.next()) {
				int id=resultSet.getInt("id");
				String name=resultSet.getString("name");
				String gender=resultSet.getString("gender");
				double salary=resultSet.getDouble("salary");
				LocalDate start=resultSet.getDate("start").toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, gender, start));
			}
			return employeePayrollList;
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to read from database");
		}
	}

	public List<EmployeePayrollData> getEmployeePayrollDataFromDB(String name) throws EmployeePayrollException {
		String sql=String.format("select * from payroll_data where name='%s'",name);
		List<EmployeePayrollData> employeePayrollList=new ArrayList<EmployeePayrollData>();
		try (Connection connection=this.getConnection()){
			Statement statement=connection.createStatement();
			ResultSet resultSet=statement.executeQuery(sql);
			while(resultSet.next()) {
				int id=resultSet.getInt("id");
				String objectname=resultSet.getString("name");
				String gender=resultSet.getString("gender");
				double salary=resultSet.getDouble("salary");
				LocalDate start=resultSet.getDate("start").toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, objectname, salary, gender, start));
			}
			return employeePayrollList;
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable to read from database");
		}
	}
	
	public int updateEmployeeData(String name, double salary) throws EmployeePayrollException {
		return this.updateEmployeePayrollDataUsingPrepredStatement(name, salary);
	}

	private int updateEmployeePayrollDataUsingStatement(String name, double salary) throws EmployeePayrollException {
		String sql=String.format("update payroll_data set salary=%.2f where name='%s'",salary,name);
		try (Connection connection=this.getConnection()){
			Statement statement=connection.createStatement();
			int rowsAffected=statement.executeUpdate(sql);
			return rowsAffected;
		} catch (SQLException e) {
			throw new EmployeePayrollException("Unable To update data in database");
		}
	}
	
	private int updateEmployeePayrollDataUsingPrepredStatement(String name, double salary) throws EmployeePayrollException {
		if(this.preparedStatement==null) {
			this.prepareStatementForEmployeePayroll();
		}
		try {
			preparedStatement.setDouble(1, salary);
			preparedStatement.setString(2, name);
			int rowsAffected=preparedStatement.executeUpdate();
			return rowsAffected;
		}catch(SQLException e) {
			throw new EmployeePayrollException("Unable to use prepared statement");
		}
	}
	
	private void prepareStatementForEmployeePayroll() throws EmployeePayrollException {
		try {
			Connection connection=this.getConnection();
			String sql="update payroll_data set salary=? where name=?";
			this.preparedStatement=connection.prepareStatement(sql);
		}catch (SQLException e) {
			throw new EmployeePayrollException("Unable to prepare statement");
		}
	}
			
}
