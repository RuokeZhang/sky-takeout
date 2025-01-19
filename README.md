# Api Docs

## Knife4j

Knife4j is a solution for integrating Swagger in Spring MVC, providing some useful annotations for developers to generate a interface doc.
When the service starts, the api will show in http://localhost:8080/doc.html

## ApiFox
1. Develop the code according to the api doc: [business api doc in json](./assets/%E8%8B%8D%E7%A9%B9%E5%A4%96%E5%8D%96-%E7%AE%A1%E7%90%86%E7%AB%AF%E6%8E%A5%E5%8F%A3.json). This is of Yapi form.
   Import it to Apifox: ![apifox-yapi](./assets/apifox-yapi.png)
2. Write code first, then get the api doc.
   Schedule auto-import from Knife4j:![apifox-knife4j](./assets/apifox-knife4j.png)




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

# Interceptor
TODO

# AutoFill
Once we update some information in the database, we leave a timestamp. And this feature is shared by all the insertion and update operations.

-   **Define a custom annotation `@AutoFill`**: Use this annotation to mark methods that need automatic filling of common fields.
-     @Target(ElementType.METHOD)  //indicates that this annotation is used in functions
  @Retention(RetentionPolicy.RUNTIME)  
  public @interface Autofill {  
  //类型为OperationType的参数可以被传进@AutoFill 这个注解
  OperationType value();  
  }

-   **Create a custom aspect class `AutoFillAspect`**: This aspect intercepts methods annotated with `@AutoFill` and uses reflection to assign values to common fields automatically.


		@Before("autoFillPointCut()")
		public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
			log.info("开始进行公共字段自动填充");
			//jointPoint用来获取当前方法的信息
			MethodSignature signature=(MethodSignature) joinPoint.getSignature();
			Autofill autofill=signature.getMethod().getAnnotation(Autofill.class);
			OperationType operationType=autofill.value();
			//获得操作的 Entity
			Object[] args = joinPoint.getArgs();
			Object entity=args[0];
			//准备赋值的数据（当前用户的 Id）
			Long currentId= BaseContext.getCurrentId();
			//通过反射来为不同的属性赋值
			if(operationType==OperationType.INSERT){
			Method setCreateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
			setCreateUser.invoke(entity, currentId);
			}
		}`

-   **Add the `@AutoFill` annotation to methods in the Mapper**: Apply this annotation to the relevant methods in your Mapper to enable the automatic filling functionality.


		//在 Mapper方法上面加上注解
		@Autofill(value = OperationType.INSERT)
		void insert(Category category);



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

# Cache
If too many people ordering dishes at the same time, it will create too much requests to the backend's database. Hence, we use Redis to cache the dishes in the backend.
Every time we make changes to the database, we want to clear the cache.

## Dish Cache
1. use a RedisTemplate in user/DishController, every time a user want to see the dishes in a category, it will first try to read from the cache.
2. use a RedisTemplate in admin/DishController. Every time we modify the dishes, we will clear the cache that has old value.

## Setmeal Cache
We use **Spring Cache** to cache setmeal.
Spring Cache is a framework, it provides annotations for cache. It have different implementations, we can easily switch to different options.
It's realized by AOP and Proxy.

**@CachePut:** Put the returned value in the cache.


    @CachePut(cacheNames="UserCache", key=#user.id")
    //this will generate a Redis key: "userCache::2"
    public User save(@RequestBody User user){
	    userMapper.insert(user);
	    return user
    }

**@Cacheable:** Before a function starts, it will first check if the data is in the cache. If not, call this function and put the returned value in the cache.


    @Cahceable(cacheNames="UserCache", key=#id")
    public User getById(Long id){
	    return userMapper.getById(id);
	}

