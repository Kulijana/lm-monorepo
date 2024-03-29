# lm-monorepo
This repository contains the needed services to run and test our distributed lock manager.

# Build

To build our solution, we can use maven to build the solution. We can use `maven clean install` to build the solution, which may need to be done for each project separately. If that is the case, common project should be built first.

# Configuration
ClientService, LockManager and StoreService contain `application.properties` file which can be used to configure a database source as well as ports for the services that expose them. 
The current values in the file are:
- spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:3306/lm_serialized
- spring.datasource.username=springuser
- spring.datasource.password=password
- spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# Run
Services can be started using either an IDE or .jar files generated after the building process. LockManager and StoreService need to be up and running before running ClientService.
ClientService takes arguments that define the values for a scenario they create. If no arguments are given, the following values are assumed:

- serviceCount = 5
- productCount = 100
- productsToBuyCount = 5
- productAmount = 200
- customerBalance = 5000

We can place 3 values as our program arguments, which would affect the first three values from the list, if we provide 5, then all 5 will be changed in the order given.

# Test
Performing tasks given in the Run chapter, we effectively run our integration test. If we however want to execute unit tests, we can do that from the LockManager project by running `maven clean test`.