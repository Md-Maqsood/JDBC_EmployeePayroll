package com.bridgelabs.employeepayrollservicejdbc;

import java.time.LocalDate;

public class EmployeePayrollData {
	private int id;
	private String name;
	private double salary;
	private String gender;
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
				+ ", start=" + start + "]";
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
