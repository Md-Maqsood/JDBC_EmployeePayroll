package com.bridgelabs.employeepayrollservicejdbc;

public class ComputationResult {
	public ComputationType type;
	public double femaleResult;
	public double maleResult;

	public ComputationResult(ComputationType type, double femaleResult, double maleResult) {
		super();
		this.type = type;
		this.femaleResult = femaleResult;
		this.maleResult = maleResult;
	}

}
