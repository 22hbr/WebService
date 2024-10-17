package com.springboot.taxcalculator;

public class TaxResponse {
    private double income;
    private double tax;

    public TaxResponse(double income, double tax) {
        this.income = income;
        this.tax = tax;
    }

    public double getIncome() {
        return income;
    }

    public double getTax() {
        return tax;
    }
}
