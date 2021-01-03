## How to generate a new keystore?

```bash
$JAVA_HOME/bin/keytool -genkey -alias <name> -keyalg RSA -keystore <name> -keysize 2048 -validity 7000
```

## How was debug keystore generated?

```
Command line args:
[-genkey, -alias, debug, -keyalg, RSA, -keystore, debug.jks, -keysize, 2048, -validity 7000]

Enter keystore password: debug!
Re-enter new password: debug!

What is your first and last name?
  [Unknown]:  Developer
What is the name of your organizational unit?
  [Unknown]:  Engineering
What is the name of your organization?
  [Unknown]:  AppifyHub
What is the name of your City or Locality?
  [Unknown]:  GitHub
What is the name of your State or Province?
  [Unknown]:  Global
What is the two-letter country code for this unit?
  [Unknown]:  US
Is CN=Developer, OU=Engineering, O=AppifyHub, L=GitHub, ST=Global, C=US correct?
  [no]:  yes

Generating 2.048 bit RSA key pair and self-signed certificate (SHA256withRSA) with a validity of 90 days
        for: CN=Developer, OU=Engineering, O=AppifyHub, L=GitHub, ST=Global, C=US
```