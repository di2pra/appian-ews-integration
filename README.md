
# Appian EWS Integration - Send Email
Send email using EWS Integration with Appian

## Send Email Smart Service
### Node Inputs
| Input             | Data Type | Required | Description |
| ----------------- |:---------:|:--------:| ----------- |
| Service URL          | Text      | Yes      | URL to the MS Exchange EWS endpoint |
| Domain          | Text      | No      | Domain name to use for NTLM authentication |
| SCS External System Key    | Text      | Yes      | The secure credential store key as defined in the Administration Console. Fields for both “username” and “password” are required. The credentials for the proxy are optional: “proxyUsername” and “proxyPassword” |
| Connected By Proxy       | Boolean      | Yes       | Indicates if the connection to the EWS services are going through a proxy |
| Proxy URL       | Boolean      | Yes       | URL of the proxy |
| Proxy Port       | Boolean      | Yes       | Port to connect to the proxy |
| Proxy Domain      | Boolean      | Yes       | Domain name to use for NTLM authentication |
| Sender Display Name | Text    | No      | Sender alias (if needed to send on behalf of) |
| Sender Email | Text    | No      | Sender Email (if needed to send on behalf of) |
| Recipients | List of Text    | Yes      | List of recipients of the email |
| CC Recipients         | List of Text      | No | List of CC recipients of the email |
| BCC Recipients         | List of Text      | No      | List of CCI recipients of the email |
| Subject         | Text      | No      | The subject of the email |
| Body Type HTML         | Boolean      | Yes      | Set to TRUE if the body is an html |
| Body         | Document | Yes      | Text Document containing the email body text |

### Node Outputs
| Output             | Data Type | Description |
| ----------------- |:---------:| ----------- |
| Error Occurred        | Boolean   | TRUE if an error occurred while sending email |
| Error Message         | Text      | Detailed message of the error |