<link href="style.css" rel="stylesheet"></link>

# Chatter - Project for API Security Study - Reversion 1

## Important points

- Use threat-modelling with STRIDE to identify threats to your API. Select appropriate security controls for each type of threat.
- Apply rate-limiting to mitigate DoS attacks. Rate limits are best enforced in a load balancer or reverse proxy but can also be applied per-server for defense in depth.
- Enable HTTPS for all API communications to ensure confidentiality and integrity of requests and responses. Add HSTS headers to tell web browser clients to always use HTTPS.
- Use authentication to identify users and prevent spoofing attacks. Use a secure password-hashing scheme like Scrypt to store user passwords.
- All significant operations on the system should be recorded in an audit log, including details of who performed the action, when, and whether it was successful.
- Enforce access control after authentication. ACLs are a simple approach to enforcing permissions.
- Avoid privilege escalation attacks by considering carefully which users can grant permissions to other users.


## Enabling HTTPS for development

1. Download `mkcert`
    ```bash
    brew install mkcert
    ```
2. Create and install a local CA
   ```bash
   mkcert -install
   ```
3. Generate a pkcs12 file localhost.p12 that contains a certificate and private key 
    ```bash
    mkcert -pkcs12 localhost
    ```
## Testing

1. Register a `demo` user
    ```bash
    curl -i --cacert "$(mkcert -CAROOT)/rootCA.pem" \
    -d '{"username":"demo","password":"password"}' \
    -H 'Content-Type: application/json' https://localhost:4567/users
    ```
2. Create a space with unauthenticated user
    ```bash
    curl -i --cacert "$(mkcert -CAROOT)/rootCA.pem" \
    -d '{"name":"test","owner":"demo"}' \
    -H 'Content-Type: application/json' https://localhost:4567/spaces
    ```
3. Create a space with authenticated user
    ```bash
    curl -i --cacert "$(mkcert -CAROOT)/rootCA.pem" \
    -u demo:password \
    -d '{"name":"test","owner":"demo"}' \
    -H 'Content-Type: application/json' https://localhost:4567/spaces
    ```
4. Add a user to the space as a reader
    ```bash
    curl -i --cacert "$(mkcert -CAROOT)/rootCA.pem" \
    -u demo:password \
    -d '{"username":"demo2","permissions":"r"}' \
    -H 'Content-Type: application/json' https://localhost:4567/spaces/1/members
    ```
5. Post a message
    ```bash
    curl -i --cacert "$(mkcert -CAROOT)/rootCA.pem" \
    -u demo:password \
    -d '{"author":"demo","message":"Hello World!"}' \
    -H 'Content-Type: application/json' https://localhost:4567/spaces/1/messages
    ```
6. Read a message
    ```bash
    curl -i --cacert "$(mkcert -CAROOT)/rootCA.pem" \
    -u demo2:password \
    https://localhost:4567/spaces/1/messages/1
    ```
7. View the audit logs
    ```bash
    curl pem https://localhost:4567/logs | jq
    ```
8. Forced login

  - Register a `test` user
    ```bash
    curl -i --cacert "$(mkcert -CAROOT)/rootCA.pem" \
    -d '{"username":"test","password":"password"}' \
    -H 'Content-Type: application/json' https://localhost:4567/users
    ```
  - Open browser for https://localhost:4567/chatter.html
  - When prompted, enter "test/password"
  - Enter space name and owner, and press "Create".
