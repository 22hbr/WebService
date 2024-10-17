package com.springboot.taxcalculator;

import org.springframework.web.bind.annotation.*;

@RestController
public class TaxController {

    @GetMapping("/calculate-tax")
    public String getTaxCalculation(@RequestParam double income) {
        double tax = computeTax(income);
        return String.format("收入: %.2f, 个人所得税: %.2f", income, tax);
    }

    private double computeTax(double income) {
        if (income <= 5000) {
            return 0;
        }
        double taxableIncome = income - 5000;
        return taxableIncome * 0.03;  // 假设税率为3%
    }
}
