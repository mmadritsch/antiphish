package com.da.antiphish;

import com.da.antiphish.analyse.Computer;
import com.da.antiphish.health.ComputerHealthCheck;
import com.da.antiphish.resources.AnalysisResultResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * The Application class is the main entry point and starts the Dropwizard application.
 * 
 * @author Marco Madritsch
 */
public class AntiPhishApplication extends Application<AntiPhishConfiguration> {
	private Computer computer;	// controller
	
	/**
	* Entry point of the application.
	* @param args	the arguments
	*/
    public static void main(final String[] args) throws Exception {
        new AntiPhishApplication().run(args);
    }
    
    /**
	* Returns the name of the application.
	* @return	the name of the application as string
	*/
    @Override
    public String getName() {
        return "AntiPhish";
    }
    
    /**
	* Initializes the controller for the analysis.
	* @param bootstrap	the pre-start application environment, containing everything required to bootstrap a Dropwizard command.
	*/
    @Override
    public void initialize(final Bootstrap<AntiPhishConfiguration> bootstrap) {
    	computer = new Computer();
    }
    
    /**
	* Runs the Dropwizard application.
	* @param configuration	the associated configuration object
	* @param environment	the Dropwizard application's environment object
	* 
	*/
    @Override
    public void run(final AntiPhishConfiguration configuration,
                    final Environment environment) {
    	
    	// creating resource and health check
    	final AnalysisResultResource resource = new AnalysisResultResource(computer);
    	final ComputerHealthCheck computerHealthCheck = new ComputerHealthCheck(computer, "http://www.google.com");
    	
    	// register resource and health check
    	environment.jersey().register(resource);
    	environment.healthChecks().register("computer", computerHealthCheck);
    }
}
