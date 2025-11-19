package com.hdsoft.configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.CacheControl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import com.google.gson.JsonObject;
import com.hdsoft.common.Database;
import com.hdsoft.models.VaultService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@EnableScheduling
@EnableAsync
@PropertySource( { "classpath:hashicorp.properties", "classpath:ldap.properties", "classpath:application.properties" })  
@ComponentScan({ "com.hdsoft.common", "com.hdsoft.utils",
"com.hdsoft.controllers", "com.hdsoft.models", "com.hdsoft.threads", "com.hdsoft.solace"
}) 
public class WebConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer, Database  
{
	/*@Bean
    public CookieSerializer cookieSerializer()
	{
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setSameSite("Lax"); // Set to "Lax", "Strict", or "None" as needed
        return cookieSerializer;
    } 
	*/
	
	/* @Bean
	 public FilterRegistrationBean<SameSiteCookieFilter> sameSiteCookieFilter() 
	 {
	        FilterRegistrationBean<SameSiteCookieFilter> registrationBean = new FilterRegistrationBean<SameSiteCookieFilter>();
	        registrationBean.setFilter(new SameSiteCookieFilter());
	        registrationBean.addUrlPatterns("/*");
	        return registrationBean;
	 }*/
	
   @Bean
   public InternalResourceViewResolver viewResolver()	
   { 
		InternalResourceViewResolver vr = new InternalResourceViewResolver();
		vr.setPrefix("/Views/");
		vr.setSuffix(".jsp");		
		return vr;
   }

   @Bean
   public CommonsMultipartResolver multipartResolver() 
   {
       CommonsMultipartResolver resolver=new CommonsMultipartResolver();
       resolver.setDefaultEncoding("utf-8");
       resolver.setMaxUploadSize(50 * 1024 * 1024);  
       resolver.setMaxUploadSizePerFile(50 * 1024 * 1024);
       return resolver;
   }
   
   public void addResourceHandlers(ResourceHandlerRegistry registry)
   {
       registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/statics/").setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
      
       registry.addResourceHandler("/File_Uploads/**").addResourceLocations("classpath:/File_Uploads/").setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
   }   
  
   @Bean
   public HikariDataSource getDataSource()
   {
	   HikariDataSource datasource = new HikariDataSource();

	   try
	   {
		   HikariConfig config = new HikariConfig();
		   
		   config.setDriverClassName(Database_Driver);
		   config.setJdbcUrl(Connection_URL);  
		   config.setUsername(DB_User); 
		   config.setPassword(DB_Pass); 
		   
		   config.setMaximumPoolSize(200); 
		   config.setMinimumIdle(20);
		   config.setIdleTimeout(600000);  
		   config.setMaxLifetime(1800000);
		   config.setConnectionTimeout(120000);
		   
		   config.addDataSourceProperty("cachePrepStmts", "true");
	       config.addDataSourceProperty("prepStmtCacheSize", "250");
	       config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
	       config.setAutoCommit(true);
	       
	       if(DB_User.equalsIgnoreCase("DVS_TZ_RTSIS_APP") || DB_User.equalsIgnoreCase("DVS_TZ_RTSIS_ACCT1"))  // UAT or prod
	  	   {
			    VaultService vault = new VaultService();
			   
			    String token = vault.getVaultToken_For_DB();
			   
			    JsonObject js = vault.AccessPassword_For_DB(token);
          	 
	          	if(js.has("data"))
	          	{
	          		String Password = js.get("data").getAsJsonObject().get("password").getAsString();
	          		 
	          		config.setPassword(Password); 
	          		
	          		vault.Update_DB_Password(config, Password);
	          	}
	  	   }
	       
	       datasource = new HikariDataSource(config);
	   }
	   catch(Exception ex)
	   {
		   System.out.println("db pass exception::::: "+ex.getLocalizedMessage());
	   }
	  
	   return datasource;	
   }
   
   protected void configure(final HttpSecurity http) throws Exception 
   {
	    http
       .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
       .and()
       .headers()
       .frameOptions().sameOrigin().httpStrictTransportSecurity().disable()
       .defaultsDisabled().contentTypeOptions()
       .and()
       .xssProtection().block(false)       
       .and()     
       .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy","default-src 'self'"))
	   .addHeaderWriter(new StaticHeadersWriter("X-WebKit-CSP","default-src 'self'"))
	   .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN))
	   .addHeaderWriter(new StaticHeadersWriter("X-Custom-Security-Header","header-value"))
	   .contentSecurityPolicy("script-src 'self'");
   }
 
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception 
   {
       http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    
       return http.build();
   }
   
   /**/
   
  /* @Bean
   public CookieSerializer cookieSerializer() {
       DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
       cookieSerializer.setSameSite("Strict"); // or "Lax", or "None" if using secure cookies
       return cookieSerializer;
   }*/
   
   @Bean
   public ThreadPoolTaskScheduler threadPoolTaskScheduler() 
   {
       ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
       threadPoolTaskScheduler.setPoolSize(500);
       return threadPoolTaskScheduler;
   }
    
   @Bean
   public RestTemplate getRestTemplate() 
   {
      return new RestTemplate();
   }
   
   
  /* @Value("${spring.ldap.url}")
   private String ldapurl;
   
   @Value("${spring.ldap.url}")
   private String ldapbase;  
   
   @Value("${spring.ldap.socket}")
   private String ldapsocket;
   
   @Value("${spring.ldap.userdn}")
   private String ldapuserdn;
   
   @Value("${spring.ldap.userdnpass}")
   private String ldapuserdnpass;
   
   @Value("${spring.ldap.certificate}")
   private static String ldapcertificate; 
   
   @Value("${spring.ldap.certpass}")
   private static String ldapcertpass;  
   
   @Bean
   public LdapContextSource ldapContextSource() throws Exception 
   {
	   LdapContextSource contextSource = new LdapContextSource();
	   
	   try
	   {
		   contextSource.setUrl(ldapurl);
	       contextSource.setBase(ldapbase);
	       contextSource.setUserDn(ldapuserdn);
	       contextSource.setPassword(ldapuserdnpass);
	       contextSource.setPooled(true); // Enable connection pooling

	       System.setProperty("javax.net.ssl.trustStore", ldapcertificate);
	       System.setProperty("javax.net.ssl.trustStorePassword", ldapcertpass);
 
	       //Hashtable<String, Object> env = new Hashtable<>();
	       
	       //env.put(ldapsocket, CustomSSLSocketFactory.class.getName());
	      // contextSource.setBaseEnvironmentProperties(env);
	   }
	   catch(Exception ex)
	   {
		   ex.printStackTrace();
	   }
      
       return contextSource;
   }
   
   @Bean
   public LdapTemplate ldapTemplate() throws Exception {
       return new LdapTemplate(ldapContextSource());
   }*/

@Override
public boolean ss(String pass) {
	// TODO Auto-generated method stub
	return false;
}

/*
private static final Logger logger = LogManager.getLogger(WebConfiguration.class);

public HikariDataSource dataSource = new HikariDataSource();

@Value("${jdbc.url}")
private String jdbcUrl;

@Value("${jdbc.driver}")
private String jdbcDriver;

@Value("${jdbc.username}")
private String username;

@Value("${jdbc.password}")
private String password;

@Bean
public HikariDataSource dataSource() {
    //if (dataSource == null) {
        dataSource = createDataSource(jdbcUrl, username, password);
    //}
    return dataSource;
}

private HikariDataSource createDataSource(String dbUrl, String username, String password) {
    HikariConfig config = new HikariConfig();
    
    try
    {
    	System.out.println(jdbcDriver); System.out.println(dbUrl); System.out.println(username); System.out.println(password);
        config.setDriverClassName(jdbcDriver);
        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(password);
    }
    catch(Exception ex)
    {
    	ex.printStackTrace();
    }
    
    return new HikariDataSource(config);
}

public void updatePassword(String newPassword) {
   
    logger.info("Updating database password.");
    
    try {
        // Create a new datasource with the updated password
        HikariDataSource newDataSource = createDataSource(jdbcUrl, username, newPassword);
        
        // Close the old datasource
        if (dataSource != null) {
            dataSource.close();
            logger.info("Closed old datasource.");
        }

        // Update to the new datasource
        dataSource = newDataSource;
        logger.info("Password updated successfully.");
    } catch (Exception e) {
        logger.error("Error updating password: ", e);
    }
}
 */
}

