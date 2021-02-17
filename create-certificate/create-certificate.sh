#!/bin/bash

echo creating "${1}keystore"

keytool -keystore "${1}keystore" -genkey -alias "${1}"

echo creating "${1}keystore" certificate request
keytool -keystore "${1}keystore" -certreq -alias "${1}" -keyalg rsa -file "${1}.csr"

echo verify "${1}keystore" with CA
openssl  x509  -req  -CA cert.pem -CAkey key.pem -in "${1}.csr" -out "${1}.cer"  -days 365  -CAcreateserial

echo import CA to "${1}keystore"
keytool -import -keystore "${1}keystore" -file cert.pem -alias theCARoot

echo import certificate to "${1}keystore"
keytool -import -keystore "${1}keystore" -file "${1}.cer" -alias "${1}"