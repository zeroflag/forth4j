import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ForthTest {
  private Forth forth;
  private ByteArrayOutputStream stout = new ByteArrayOutputStream();
  private PrintStream originalOut;

  @Before
  public void setUp() throws Exception {
    forth = new Forth(8192);
    forth.eval(new String(Files.readAllBytes(Paths.get(Forth.class.getResource("core.forth").toURI()))));
    originalOut = System.out;
    System.setOut(new PrintStream(stout));
  }

  @After
  public void tearDown() throws Exception {
    assertEquals(0, forth.stackSize());
    System.setOut(originalOut);
  }

  @Test
  public void testArithmetic() {
    assertEquals(3, evalPop("1 2 +"));
    assertEquals(12, evalPop("3 4 *"));
    assertEquals(4, evalPop("6 2 -"));
    assertEquals(-3, evalPop("7 10 -"));
    assertEquals(2, evalPop("100 50 /"));
    assertEquals(101, evalPop("100 1+"));
  }

  @Test
  public void testLogic() {
    assertEquals(true, evalPop("true true and"));
    assertEquals(false, evalPop("true false and"));
    assertEquals(false, evalPop("false true and"));
    assertEquals(false, evalPop("false false and"));

    assertEquals(true, evalPop("true true or"));
    assertEquals(true, evalPop("true false or"));
    assertEquals(true, evalPop("false true or"));
    assertEquals(false, evalPop("false false or"));
  }

  @Test
  public void testJuggling() { // http://sovietov.com/app/forthwiz.html
    assertEquals(asList(2, 1), evalGetStack("1 2 swap"));
    assertEquals(asList(1, 2, 1), evalGetStack("1 2 over"));
    assertEquals(asList(3, 3), evalGetStack("3 dup"));
    assertEquals(asList(3, 4, 3, 4), evalGetStack("3 4 2dup"));
    assertEquals(asList(6), evalGetStack("5 6 nip"));
    assertEquals(asList(7), evalGetStack("7 8 drop"));
    assertEquals(emptyList(), evalGetStack("6 5 2drop"));
    assertEquals(asList(2, 3, 1), evalGetStack("1 2 3 rot"));
    assertEquals(asList(3, 1, 2), evalGetStack("1 2 3 -rot"));
    assertEquals(asList(2, 1, 2), evalGetStack("1 2 tuck"));
  }

  @Test
  public void testCmp() {
    assertEquals(false, evalPop("1 2 ="));
    assertEquals(true, evalPop("2 2 ="));
    assertEquals(true, evalPop("1 2 !="));
    assertEquals(false, evalPop("1 1 !="));

    assertEquals(true, evalPop("10 20 <"));
    assertEquals(false, evalPop("10 10 <"));
    assertEquals(false, evalPop("13 10 <"));

    assertEquals(true, evalPop("30 20 >"));
    assertEquals(false, evalPop("10 10 >"));
    assertEquals(false, evalPop("4 10 >"));

    assertEquals(true, evalPop("10 20 <="));
    assertEquals(true, evalPop("10 10 <="));
    assertEquals(false, evalPop("13 10 <="));

    assertEquals(true, evalPop("30 20 >="));
    assertEquals(true, evalPop("10 10 >="));
    assertEquals(false, evalPop("4 10 >="));
  }

  @Test
  public void testUtils() {
    assertEquals(123, evalPop("123 456 min"));
    assertEquals(12, evalPop("654 12 min"));
    assertEquals(456, evalPop("123 456 max"));
    assertEquals(654, evalPop("654 12 max"));
  }

  @Test
  public void testWhile() {
    eval(": ctn-while " +
            "begin " +
            "    dup 0 >= " +
            "while " +
            "    dup . 1 - " +
            "repeat " +
            "drop ; " +
            "5 ctn-while");
    assertEquals("5\n4\n3\n2\n1\n0\n", stdout());
  }

  @Test
  public void testUntil() {
    eval(": ctn-until " +
            " begin " +
            "    dup . " +
            "    1 - dup " +
            "  0 < until " +
            "  drop ; " +
            "5 ctn-until");
    assertEquals("5\n4\n3\n2\n1\n0\n", stdout());
  }

  @Test
  public void testRStack() {
    eval("12 >r 14 >r r> r>");
    assertEquals(12, forth.pop());
    assertEquals(14, forth.pop());
    eval("42 >r i r>");
    assertEquals(42, forth.pop());
    assertEquals(42, forth.pop());
    eval("7 >r 8 >r i j");
    assertEquals(Arrays.asList(8, 7), forth.stack());
    forth.eval("clear");
  }

  @Test
  public void testMemory() {
    eval("42 800 !");
    assertEquals(42, evalPop("800 @"));
  }

  @Test
  public void testDo() {
    eval(": ctn-do 0 do i . loop ; 10 ctn-do");
    assertEquals("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n", stdout());
  }

  private String stdout() {
    return new String(stout.toByteArray());
  }

  private List<Object> evalGetStack(String script) {
    forth.eval(script);
    List<Object> result = new ArrayList<>();
    int size = forth.stackSize();
    for (int i = 0; i < size; i++) {
      result.add(forth.pop());
    }
    Collections.reverse(result);
    forth.eval("clear");
    return result;
  }

  private Object evalPop(String script) {
    eval(script);
    return forth.pop();
  }

  private void eval(String script) {
    forth.eval(script);
  }
}