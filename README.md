# BankTransfer

For production ready code , following steps could be taken 

1. Introduce more logging especially around the actual transfer code so that the logs could be studied using tools like Splunk in case of any issues . 
2. Detailed logging with user IP Address can help later 
3. More exception handling around the transfer code and around the notification code will be useful . 
4. More unit tests to test the transfer with corner cases will make the code more robust and error free.
5. Load testing the code when multiple threads work on the transfer and sending notifications will help . Tools like Jmeter can be used to simulate .
6. Negative testing should be done to check if the code is behaving properly . 
7. End to end integration tests needs to be done to verify the functionality. 


Assumptions in the code 
There is only EmailNotificationService which implements NotificationService in the system, therefore have autowired it in the Repository implementation .
Have tried to put the account related operations like deposit in the Account class itself .
Have created models to hold the data for sender and receiver notifications .