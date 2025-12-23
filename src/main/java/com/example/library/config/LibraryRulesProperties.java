package com.example.library.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "library.rules")
public class LibraryRulesProperties {

    @Min(1)
    @Max(50)
    private int maxActiveLoans = 3;

    @Min(1)
    @Max(365)
    private int loanDurationDays = 14;

    public int getMaxActiveLoans() {
        return maxActiveLoans;
    }

    public void setMaxActiveLoans(int maxActiveLoans) {
        this.maxActiveLoans = maxActiveLoans;
    }

    public int getLoanDurationDays() {
        return loanDurationDays;
    }

    public void setLoanDurationDays(int loanDurationDays) {
        this.loanDurationDays = loanDurationDays;
    }
}
