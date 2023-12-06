# README

## Generate a Self-Signed Certificate

```bash
# create root CA key and cert
openssl req -newkey rsa:2048 -nodes \
-sha256 \
-x509 -days 365 \
-subj "/CN=ca.pheely.com/C=CA/ST=Ontario/L=Markham" \
-keyout rootCA.pem \
-out rootCA.crt

# generate a private key
openssl genrsa -out dev.pem 2048

# create a csr.conf
tee csr.conf <<EOF
[ req ]
default_bits = 2048
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn

[ dn ]
C = CA
ST = Ontario
L = Markham
O = Pheely
OU = Pheely Dev
CN = dev.pheely.com

[ req_ext ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = dev.pheely.com
DNS.2 = www.dev.pheely.com
EOF

# create a csr using the dev key
openssl req -new -key dev.pem -out dev.csr -config csr.conf

# create a cert.conf for SSL cert
tee cert.conf <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = dev.pheely.com
EOF

# sign dev cert with self-signed ca
openssl x509 -req \
    -in dev.csr \
    -CA rootCA.crt -CAkey rootCA.pem \
    -CAcreateserial -out dev.crt \
    -days 365 \
    -sha256 -extfile cert.conf
    
# generate keystore in pkcs12 format
openssl pkcs12 -export -name pheely-dev -out pheely-dev.p12 -inkey dev.pem -in dev.crt
```