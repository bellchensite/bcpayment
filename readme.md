# Project information
This is a springboot application, springboot version:2.2.5, JDK 1.8, with Maven to build up, please make sure all dependencies are correctly down loaded.
Any issue on running, just email me :)

# How to run
* In Eclipse
Import this project as Maven project, wait for all dependency libraries download finished.
Open com.bc.pay.PaymentsApplication.java, right click -> run as -> JAVA application

* In Maven
Open CMD window
cd __Project_Root_folder__  
mvn spring-boot:run

* In JAVA executable jar(the executable jar file has been included in the project root folder)
java -jar Payments-0.0.1-SNAPSHOT.jar

# How to specify the initial payment file?
Open project folder /src/main/resource/application.yml
payment.service:
  paymentFile: classpath:payment.txt
Can remove this file, or change to another files if you like.

# Change the printing interval
Printing payments result to console was set to every 60s, this is specified in application.yml -> payment.service.printingInterval (in milliseconds)

# Test
Test package has been included in to src/test/java folder
Open com.bc.pay.service.PaymentServiceTest.java, right click -> run as -> JUnit( please make sure you are using JUNIT5 in run configuration))

# About running logic
1. It has strong format validation, you could only input <CURRENCYCODE(in uppercase) Amount> format, otherwise it will hint out the format incorrect.
e.g. "USd 1000", it will say "Sorry, detected an illegal format: USd100, this line would be ignored. The correct format should be like: <USD 100>", 
but that just a hint, you could keep continue to input the right value.
2. Input "quit" to end the program running.
3. This version contains USD exchange convention, the USD exchange rates are configured in application.yml: payment.service.exchangeRateMap, can add up 
meaningful exchange rate if you like, e.g. "TWD": 100

Enjoy:)
