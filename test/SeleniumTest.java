import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import module.OnStart;

import org.hadatac.utils.ConfigProp;

import play.test.TestServer;
import play.test.Helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;

public class SeleniumTest {

   private WebDriver driver;
   private TestServer server;

   final private String name = "Test User";
   final private String keyUser = ConfigProp.getTestUsername();
   final private String keyPassword = ConfigProp.getTestUserPass();
   final private String webDriverPath = ConfigProp.getWebDriverPath();

   final private int waitTime = 5000;

   @Before
   public void setup() throws Exception {
      System.out.println("Setting up SeleniumTest...");

      // Test user configuration
      if(keyUser.equals("")){
         throw new IOException("Missing test username, please set it in hadatac.config");
      }

      if(keyPassword.equals("")){
         throw new IOException("Missing test password, please set it in hadatac.config");
      }

      if(webDriverPath.equals("")){
         throw new IOException("Missing web driver, please set it in hadatac.config");
      }

      // Setup WebDriver
      System.setProperty("webdriver.chrome.driver", webDriverPath);
      driver = new ChromeDriver();
      driver.manage().window().maximize();

      // Start HADatAc
      new OnStart();

      // Sign up admin user
      if(ConfigProp.getSignUpBool()){
         System.out.println("Creating admin user...");

         server = Helpers.testServer(9000);
         Helpers.running(server, () ->
         // Code block run once server is started
         {
            try{
               driver.get("http://localhost:9000/hadatac/signup");
               Thread.sleep(waitTime);

               // Set Credentials
               WebElement element = driver.findElement(By.name("name"));
               element.sendKeys(name);

               element = driver.findElement(By.name("email"));
               element.sendKeys(keyUser);

               element = driver.findElement(By.name("password"));
               element.sendKeys(keyPassword);

               element = driver.findElement(By.name("repeatPassword"));
               element.sendKeys(keyPassword);
               element.sendKeys(Keys.RETURN);
               Thread.sleep(waitTime);

               // Log Out
               driver.get("http://localhost:9000/hadatac/logout");
            }
            catch(InterruptedException e){
               System.out.println(e.getMessage());
               fail(e.getMessage());
            }
         });
      }

      System.out.println("Finished setup!");
   }

   @Test
   public void loginTest() {
      System.out.println("Running SeleniumTest.loginTest()...");
      
      server = Helpers.testServer(9000);
      Helpers.running(server, () ->
      // Code block run once server is started
      {
         try{
            driver.get("http://localhost:9000/hadatac/login");
            Thread.sleep(waitTime);

            //Entering user to log.
            //WebElement is a class that it allows instantiate objects to execute some action using a HTML element.
            WebElement login = driver.findElement(By.name("email"));
            login.sendKeys(keyUser);

            //String keyPassword = "\t"+"pwd";
            //Entering password to log; \t separate and tabs the field
            WebElement password = driver.findElement(By.name("password"));
            password.sendKeys(keyPassword);

            //The parameter Keys.RETURN is used.
            driver.findElement(By.name("password")).sendKeys(Keys.RETURN);
            Thread.sleep(waitTime);
            WebElement header = driver.findElement(By.xpath("//h4[contains(text(), 'Data/Metadata Search')]"));
            assertTrue("Page title differs from expected", header != null);

            header = driver.findElement(By.xpath("//h4[contains(text(), 'Metadata Recording and Data Upload')]"));
            assertTrue("Page title differs from expected", header != null);

            header = driver.findElement(By.xpath("//h4[contains(text(), 'Knowledge Management')]"));
            assertTrue("Page title differs from expected", header != null);

            header = driver.findElement(By.xpath("//h4[contains(text(), 'Data/Metadata Retrieve and Download')]"));
            assertTrue("Page title differs from expected", header != null);

            header = driver.findElement(By.xpath("//h4[contains(text(), 'Documentation')]"));
            assertTrue("Page title differs from expected", header != null);
         }
         catch(InterruptedException e){
            System.out.println(e.getMessage());
            fail(e.getMessage());
         }
      });

      System.out.println("SeleniumTest.loginTest()...   Passed!");
   }

   @After
   public void tearDown() throws Exception{
      driver.quit();
      server.stop();
      System.out.println("Finished running SeleniumTest");
   }
}
