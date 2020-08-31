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

   final private String keyUser = ConfigProp.getTestUsername();
   final private String keyPassword = ConfigProp.getTestUserPass();
   final private String webDriverPath = ConfigProp.getWebDriverPath();

   @Before
   public void setup() throws Exception {

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
      server = Helpers.testServer(9000);
   }

   @Test
   public void loginTest() {
      Helpers.running(server, () ->
      // Code block run once server is started
      {
         try{
            driver.get("http://localhost:9000/hadatac/login");
            Thread.sleep(5000);

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
            Thread.sleep(5000);
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
            fail(e.getMessage());
         }
      });
   }

   @After
   public void tearDown() throws Exception{
      driver.quit();
      server.stop();
   }
}
