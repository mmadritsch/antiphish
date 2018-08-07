package com.da.antiphish.health;

import com.codahale.metrics.health.HealthCheck;
import com.da.antiphish.analyse.Computer;

/**
 * ComputerHealthCheck checks the correct functioning of the entire analysis process at runtime of the application.
 * 
 * @author Marco Madritsch
 */
public class ComputerHealthCheck extends HealthCheck {
	private final Computer computer;
	private final String url;

    public ComputerHealthCheck(Computer computer, String url) {
        this.computer = computer;
        this.url = url;
    }

    @Override
    protected Result check() throws Exception {
    	double result = computer.analyse(url);
    	
        if(result <= 1.0 && result >= 0.0) {
            return Result.healthy();
        }
        return Result.unhealthy("Can not analyse " + url);
    }
}
