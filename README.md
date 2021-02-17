# Project 2

## Requirements 
- Someone working at a hospital has access to a medifcal record only if he/she treats the patient or is related in some other way to his/her work.
- Patient can access his/her own file
- An audit log must be kept that logs all access to a medical record. 
- Socialstyrelsen should be able to destroy the audit file
- Need to write in Java
- Need client and server implementations from Project 1. 
- Some form of two-factor authentication
- All certificates needs to have a CA


## Rules for access
- A patient is allowed to read own list of records
- A nurse may read and write to all records associated with them, and also read all records associated with the same division.
- A doctrom may read and write to all records. Also read all records in the same division. In addition, the doctor can create new records for a patient provided that the doctor is treating the patient. When creating, the doctor associats a nurse with the record.

# Useful links:
1. https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#enabling-tls-1.3
2. https://docs.oracle.com/javase/8/docs/api/javax/net/package-summary.html
3. https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/package-summary.html
4. https://docs.oracle.com/javase/8/docs/api/javax/security/cert/package-summary.html
