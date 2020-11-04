package com.bridgelabs.employeepayrollservicejdbc;

import java.time.LocalDate;

public class EmployeePayrollData {
	private int id;
	private String name;
	private double salary;
	private String gender;
	private String company;
	private String address;
	private String phone_number;
	
	public EmployeePayrollData(int id, String name, double salary, String gender, String company, String address,
			String phone_number, LocalDate start) {
		this(id, name, salary, gender, start);
		this.company = company;
		this.address = address;
		this.phone_number = phone_number;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	private LocalDate start;
	public EmployeePayrollData(int id, String name, double salary, String gender) {
		this.id = id;
		this.name = name;
		this.salary = salary;
		this.gender = gender;
	}
	
	public EmployeePayrollData(int id, String name, double salary, String gender, LocalDate start) {
		this(id,name,salary,gender);
		this.start=start;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public LocalDate getStart() {
		return start;
	}

	public void setStart(LocalDate start) {
		this.start = start;
	}
	
	
	
	@Override
	public String toString() {
		return "EmployeePayrollData [id=" + id + ", name=" + name + ", salary=" + salary + ", gender=" + gender
				+ ", company=" + company + ", address=" + address + ", phone_number=" + phone_number + ", start="
				+ start + "]";
	}

	@Override
	public boolean equals(Object o) {
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		EmployeePayrollData that=(EmployeePayrollData) o;
		return id== that.id &&
				Double.compare(that.salary, salary)==0 &&
				name.equals(that.name);		
	}
}
