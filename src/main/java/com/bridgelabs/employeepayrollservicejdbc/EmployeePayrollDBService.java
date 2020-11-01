package com.bridgelabs.employeepayrollservicejdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmployeePayrollDBService {
	private static final Logger logger=LogManager.getLogger(EmployeePayrollDBService.class);
	private static final String JDBC_URL="jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
	private static final String USERNAME="root";
	private static final String PASSWORD="abcd1234";
	public boolean establishConnection() {
		Connection connection;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			logger.info("Driver loaded");
		}catch(ClassNotFoundException e) {
			return false;
		}
		listDrivers();
		try {
			connection=DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
			logger.info("Connection is established");
		}catch(Exception e) {
			return false;
		}
		return true;
	}
	private static void listDrivers() {
		Enumeration<Driver>  driverList= DriverManager.getDrivers();
		while (driverList.hasMoreElements()) {
			Driver driverClass=(Driver) driverList.nextElement();
			logger.info("   "+driverClass.getClass().getName());
		}
	}
		
}
