# JWT
When an employee login, api /admin/employee/login will return a JWT token.
Once the employee send another request, this token will be carried in the request's header

## JWT structrue

JWT means Json Web Token. It's a json object used in authentication.
JWT's serialized form:
[header].[payload].[signature]

    > header:  
    {  
     "alg" : "HS256",  
     "typ" : "JWT"  
    }  
   	Payload:  
   	{  
   	 "id" : 123456789,  
   	 "name" : "Joseph"  
   	}  
   	  
   	Secret: GeeksForGeeks

Its corresponding web token:

    base64UrlEncode(header) + "." + base64UrlEncode(payload) + "." + base64UrlEncode(HMAC-SHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret))

1. Header specified the crypto algorithm used in JWT.
2. Payload carries the actual data.
3. Siganature: base64UrlEncode(HMAC-SHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret))

## JJWT Usage
Import jjwt-0.9.1.jar in the project, then use Jwts.builder() to create a JWT token.