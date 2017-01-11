import static org.junit.Assert.*;
import static org.junit.Assume.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.AssumptionViolatedException;

public class ExampleTest {

	
// Assert Equals Test of Addition
  @Test
  public void testSum() {
    int a = 1 + 1;
    assertEquals(2, a);
  }
  
//Assert False Test of String
  @Test
  public void testString() {
    String str = "Hello world";
    assertFalse(str.isEmpty());
  }

// Assert Not Null Test of Array
  @Test
  public void testArray() {
	  List<String> values = new ArrayList<String>();
	  values.add("Label");
 	  values.add("Type");
 	  values.add("Cohort");
 	  values.add("Study");
	  assertNotNull(values);
  }

// Assert True Test of Boolean
  @Test
  public void testBoolean() {
	  boolean trigger = false;
	  int i = 14;
	  assertFalse(trigger);
	  if (i > 10) {
		  trigger = true;
	  }
	  assertTrue(trigger);
  }
  
// Assert Same Test of Integers  
  @Test
  public void testSimilarity(){
	  int a = 1;
	  String b = "1";
	  assertNotSame(a,b);
	  int c = Integer.parseInt(b);
	  assertSame(a,c);
  }
  
//Assert Equals Test of Doubles  
  @Test
  public void testDouble(){
	  double a = 0.12;
	  double b = 0.4;
	  double c = b*.3;
	  double delta = 0.001;
	  assertEquals(c,a,delta);
  }

// Fail Test 
  @Test
  public void testFail(){
	  boolean a = false;
	  int i = 0;
	  while (i<10){
		  i++;
		  if (i == 11) {
			  a = true;
		  }
	  }
	  if (a){
		  fail();
	  }
  }
  
// Ignore Test  
  @Ignore("Testing Ignore") @Test
  public void testIgnore(){
	  fail();
  }
  
// assume
  
  @Test
  public void assumeTrueTest(){
	  boolean a = false;
	  assumeTrue(a);
	  int b = 2; //This code does not execute
  }

  /*
  @Test
  public void assumeThatTest(){
	  int a = 1;
	  assumeThat(a, is(1));
  }
  */
  
  @Test
  public void assumeNotNullTest(){
	  boolean a = true;
	  assumeNotNull(a);
  }
  
  
  
}