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

## What about certificates?

Public:

```
-----BEGIN CERTIFICATE-----
MIIDfTCCAmWgAwIBAgIIKgzdNt1lYf4wDQYJKoZIhvcNAQELBQAwbTELMAkGA1UE
BhMCVVMxDzANBgNVBAgTBkdsb2JhbDEPMA0GA1UEBxMGR2l0SHViMRIwEAYDVQQK
EwlBcHBpZnlIdWIxFDASBgNVBAsTC0VuZ2luZWVyaW5nMRIwEAYDVQQDEwlEZXZl
bG9wZXIwHhcNMjAxMjI2MTg0ODI0WhcNNDAwMjI1MTg0ODI0WjBtMQswCQYDVQQG
EwJVUzEPMA0GA1UECBMGR2xvYmFsMQ8wDQYDVQQHEwZHaXRIdWIxEjAQBgNVBAoT
CUFwcGlmeUh1YjEUMBIGA1UECxMLRW5naW5lZXJpbmcxEjAQBgNVBAMTCURldmVs
b3BlcjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK4jB+AqbgRwdCbN
1VKobm5roj3rlodiCvcVEwHYmP+W7cpqrKDojJoMZ859/CFs5eqUkh4ynMjhyiuu
g+Y/opFJJxYXkcaWGzyjG/fO7c/RAVPbkM9p89O1xbO2+PVQ1p3K9N2ZlpyIvInC
Xr6WYN/+giOgw8HzsH7nF+AQ5RFO/B54HsKDIMYWfJtPL0bCO3DShVivxmv4mE8R
3xmniniZAUUnN+cULr0LzziGecJLINj64jzSuP6PvXlcTaBuXBBXf2JJYqwEi41k
e6XLzLMRJYAxU/K7yo76KmxNyVSI1TOvMiJgguuhNkAfCQkFlkdUsUwvI01ziFI5
DMaH5gcCAwEAAaMhMB8wHQYDVR0OBBYEFGW567ZMxsxKPX1tvYxLKcxNV/AWMA0G
CSqGSIb3DQEBCwUAA4IBAQA2aHe4Njcm1dtt1GcMohchy4lMK8Lz3Suog2RCdd9g
j1dUwIm+s1S/W8d/hBg2L2S8/hAEXtq16e8k0HysU15m/POmOWezB2TyQ1rZWeiw
d1XxwPdcl1sxO1MDadUGIPXHZxpVtT2fpIxoA460BQ96VckMfmWxcMt0dA2Pnese
qW3nwooKR23efCVyhk+UwC0Ji4CEqDfU0Eu8bTtwxDkBJ3AOIG4oW9p4U1LfxdaE
DfdCl77B1qTD3DwjI1iFZ5IAlhkKzzs5SB20M1uD/U0CyYcodCqNXCUG1F1XT8GO
JwNdmqkVnMVuoysgpMXe7wrub0/KhUpJZXKiQ3W//udB
-----END CERTIFICATE-----
```

Private (RSA):

```
-----BEGIN RSA PRIVATE KEY-----
Proc-Type: 4,ENCRYPTED
DEK-Info: DES-EDE3-CBC,F25B9C56816562FF

VEpbasJRY6gHKhJjk01jM+JTzBRDMZ1wp3i7h/yHCXqEQI5nl8HJeRYNwQIVViSC
KBmHjMZgr//SqeEkHAYg/O+Zd9FQZfpCOQht/Pwv7SnCDNr6CbWk7ogps1hrN1/1
bt8gNrv9piGW/keP1f6nwR62c4pG9CoPCcJbOpNWNcjsdh5q0XYco7deGoz2A601
A3iZTu0QAJkLIquu4TO+6IuS0A2pYhf7+je+MxKOjeXmlbE5xc6CbOLPSZLkuPAO
1mV2vt75zeBFqKDmga1/kzNZB8XwvlIQV8a1RXBX9TS19D02jOooPLteAWMwxPJq
NjnxiBaKkhKRovZ5E6OVtPNmwI+Rg70HumjHxKKYXGejLV7drayde0ssUuDULqRY
Y306iCwWSvFYVyBUJarJkS1LKmwuoiBJK2bwl2DuxBtMpyA0D4EFeZRd6++ezchw
DDpSobUo/7kQaXQfEIYGf1pFUtDcVCWgSnyGvHoW8Uf8jTX0jYTFZnT9J+0LDzFs
Q1YpQEqGxrCtVJPbOlyLWeQxA9JsjGFclGmCvl8NsPs3pcn+kY13kLX+BasmAQI7
mu/S+/D+XxE01xgJ2kyQjlrJsEv6osfhN+bxb8li/GMPBmzeiwBZguZo70f7fbKn
oIKyrvuvMPDC2HlC6y8qunYc5WBS7H/Gqve5Mw6aJz903Qfx9nTvCOmI3vvc5RQc
1WIaE4J1S7uBZNJwFf9q1Xy47izE/C/Bdym4igp5vPcQbT7dQblkjsLzLxUYD18I
YyzNuJM7fhTn68Nyi7OkY3t3d3/54GqAmmsXagTD3GnibFMhWkXFMLwk/XTZD3YY
OB8m0GOToPAvMwEja7BRcHhLiZOFDlqa1Is+mUJdo+zsELeMJt+Hb3R9aV2GuURT
T8EgVz+7eZ3b0YgZujKT9UgmIeGBesYxMEzBduy7ARR1vyYifLDxzbnkiQyDg2IH
VwNLsdA0qAJ4r+K0LV0jya4EMOrIspaitCdINT6TbiNQzpDvo2Z7resw2yikVPHz
gwWDeIgmBAK0VXEAG/Q9frdlCdxnZsKcjC2nL/N3n0bZLbz609JW4mHCO20v+vVv
gkPw6XEsn3kMpkSbl7Qrl56PiMHsXuruy48zdXnIgshjG5XzDzzZ4NjpeJcRcCM5
eO4lqP0HeVJEcbCDnEI3rTOb2J+ExjSFMEr9eXkRHNol0mwESyUrI+aLS6MZJALr
au3ejB0ut2WWOt+QkhYtBT+LmriBQ6U1ByCJv2oG86AqHJ7sM7qBn1YLsfwkvcbm
kuSNQgzIvYkCBOXSAdANap5kpZE8f4aEVWB9NeRmi+EvporA1mgB2NUr9fmyGkb1
0Q6yxtxApF4GAsZ9RtDlxrJmDQc4GUdH9ZjrnK4Iou3Km/MDjh8PGy9Q2UdXeNEa
MT/ehLYK9MI6dCubLVXCxnLGOsFs5ATqbFWkAdQSmDUA2q6jq02mKGr2Qz/rqnXA
JTkue65BJioTq6S3WLvaC5JeLbmP1pBHVA9xoRfjAW5pYzNnqrTJt1FUUemDge+c
yekxTGd0Zl/0ovI7dMr7VTOvE/TI+vBStVIV2ysfhNeElDmuLisKjJBAij0+Q4U9
-----END RSA PRIVATE KEY-----
```
