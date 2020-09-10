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
import org.openqa.selenium.chrome.ChromeOptions;

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
      System.out.println("Setting up SeleniumTest..."); // System.err wasw not showing up in Jenkins console so System.out is prefered

      // Ensure test user configurations are setup
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
      ChromeOptions options = new ChromeOptions();
      options.addArguments("--no-sandbox");

      // This lines controls if we see the web browser or not.
      // This needs to be here for Jenkins!!
      // but you might want to remove it while developing tests so you can see the interactions
      options.addArguments("--headless");

      driver = new ChromeDriver(options);
      driver.manage().window().maximize();

      // Start HADatAc
      new OnStart();

      // Sign up admin user
      if(ConfigProp.getSignUpBool()){
         System.out.println("Creating admin user...");

         server = Helpers.testServer(9000);
         Helpers.running(server, () ->
         // Code block runs once server is started
         {
            try{
               // Goto the sign up webpage
               driver.get("http://localhost:9000/hadatac/signup");
               Thread.sleep(waitTime);

               // Set Name
               WebElement element = driver.findElement(By.name("name"));
               element.sendKeys(name);

               // Set Email
               element = driver.findElement(By.name("email"));
               element.sendKeys(keyUser);

               // Set Password
               element = driver.findElement(By.name("password"));
               element.sendKeys(keyPassword);

               // Set password again and submit
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
            // Goto the login page
            driver.get("http://localhost:9000/hadatac/login");
            Thread.sleep(waitTime);

            // Entering user email
            // WebElement is a class that it allows instantiate objects to execute some action using a HTML element.
            WebElement login = driver.findElement(By.name("email"));
            login.sendKeys(keyUser);

            // Entering password
            WebElement password = driver.findElement(By.name("password"));
            password.sendKeys(keyPassword);
            password.sendKeys(Keys.RETURN);
            Thread.sleep(waitTime);

            // Ensure the web page contains the expected headers
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
