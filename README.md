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

# WeChat Login
![wechat login process](./assets/img.png)
1. After a user click "login" in the frontend, it will do wx.login() and it returns a code, then the frontend send that code to the backend.
2. the backend use code2Session Api to send a request to Wechat Http Api. [reference](https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html)
3. the backend use HttpClient to send a request to get a **OpenId**.

## Http Client
[usage reference](https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/client/utils/package-summary.html)
Build a destination uri: it contains the hostname and the queries
public [URIBuilder](https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/client/utils/URIBuilder.html "class in org.apache.http.client.utils") addParameter([String](https://docs.oracle.com/javase/6/docs/api/java/lang/String.html?is-external=true "class or interface in java.lang") param,
[String](https://docs.oracle.com/javase/6/docs/api/java/lang/String.html?is-external=true "class or interface in java.lang") value)

    //创建GET请求  
	HttpGet httpGet = new HttpGet(uri);  
	  
	//发送请求  
	response = httpClient.execute(httpGet);
