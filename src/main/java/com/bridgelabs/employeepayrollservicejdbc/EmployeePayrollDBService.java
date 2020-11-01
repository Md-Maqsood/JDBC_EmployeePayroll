package com.bridgelabs.employeepayrollservicejdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmployeePayrollDBService {
	private static final Logger logger=LogManager.getLogger(EmployeePayrollDBService.class);
	
	public Connection getConnection() throws EmployeePayrollException {
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
			
}
