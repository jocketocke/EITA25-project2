#Step 1. Create CA cert
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365
#Step 2.
keytool -import -file cert.pem -alias firstCA -keystore clienttruststore
#Step 3.
keytool -keystore clientkeystore -genkey -alias client
#Step 4.
keytool -keystore clientkeystore -certreq -alias client -keyalg rsa -file client.csr
#Step 5.
openssl  x509  -req  -CA cert.pem -CAkey key.pem -in csr.txt -out client.cer  -days 365  -CAcreateserial
#Step 6.
keytool -import -keystore clientkeystore -file cert.pem -alias theCARoot
keytool -import -keystore clientkeystore -file client.cer -alias client
#Step 7.
keytool -list -v -keystore clientkeystore


#Step 9.
keytool -keystore serverkeystore -genkey -alias server
keytool -keystore serverkeystore -certreq -alias server -keyalg rsa -file server.csr
openssl  x509  -req  -CA cert.pem -CAkey key.pem -in csr.txt -out server.cer  -days 365  -CAcreateserial
keytool -import -keystore serverkeystore -file cert.pem -alias theCARoot
keytool -import -keystore serverkeystore -file server.cer -alias server
#Step 10.
keytool -import -file cert.pem -alias firstCA -keystore servertruststore
