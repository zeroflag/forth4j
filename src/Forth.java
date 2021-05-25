import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

public class Forth {
  private enum Mode {COMPILE, INTERPRET}
  private final Map<String, Word> dict = new LinkedHashMap<>();
  private final Set<String> immediate = new HashSet<>();
  private final Stack<Object> stack = new Stack<>();
  private final Stack<Object> rstack = new Stack<>();
  private String lastWord;
  private StringTokenizer tokenizer;
  private Mode mode = Mode.INTERPRET;
  private final Object[] heap;
  private int dp = 0;
  private int ip = 0;
  private final Word EXIT = () -> { /* marker */ };

  interface Word { void enter();}

  class ColonDef implements Word {
    private final int address;

    public ColonDef(int address) {
      this.address = address;
    }

    @Override
    public void enter() {
      rstack.push(ip);
      innerLoop(address);
      ip = (Integer) rstack.pop();
    }

    @Override
    public String toString() {
      return "xt_" + address;
    }
  }

  private void initPrimitives() {
    dict.put("+", () -> stack.push((Integer) stack.pop() + (Integer) stack.pop()));
    dict.put("-", () -> { Integer top = (Integer) stack.pop();stack.push((Integer) stack.pop() - top); });
    dict.put("*", () -> stack.push((Integer) stack.pop() * (Integer) stack.pop()));
    dict.put("/", () -> { Integer top = (Integer) stack.pop();stack.push((Integer) stack.pop() / top); });
    dict.put("and", () -> stack.push((boolean)stack.pop() & (boolean)stack.pop()));
    dict.put("or", () -> stack.push((boolean)stack.pop() | (boolean)stack.pop()));
    dict.put("not", () -> stack.push(!(boolean)stack.pop()));
    dict.put("drop", stack::pop);
    dict.put("dup", () -> stack.push(stack.peek()));
    dict.put("swap", () -> { Object a = stack.pop();Object b = stack.pop();stack.push(a);stack.push(b); });
    dict.put("clear", stack::clear);
    dict.put("=", () -> stack.push(stack.pop().equals(stack.pop())));
    dict.put("<", () -> stack.push((Integer)stack.pop() > (Integer)stack.pop()));
    dict.put("true", () -> stack.push(true));
    dict.put("false", () -> stack.push(false));
    dict.put("here", () -> stack.push(dp));
    dict.put("lit", () -> stack.push(heap[ip++]));
    dict.put(">r", () -> rstack.push(stack.pop()));
    dict.put("r>", () -> stack.push(rstack.pop()));
    dict.put("i", () -> stack.push(rstack.peek()));
    dict.put("j", () -> stack.push(rstack.get(rstack.size() - 2)));
    dict.put(",", () -> heap[dp++] = stack.pop() );
    dict.put("!", () -> heap[(Integer)stack.pop()] = stack.pop());
    dict.put("@", () -> stack.push(heap[(Integer)stack.pop()]));
    dict.put("[']", () -> stack.push(heap[ip++]));
    dict.put("immediate", () -> immediate.add(lastWord));
    dict.put("exit", EXIT);
    dict.put(".", () -> System.out.println(stack.pop()));
    dict.put("jmp#f", () -> ip += (boolean) stack.pop() ? 1 : (Integer) heap[ip]);
    dict.put("jmp", () -> ip += (Integer) heap[ip]);
    dict.put(":", () -> { lastWord = next(); dict.put(lastWord, new ColonDef(dp)); mode = Mode.COMPILE; });
    dict.put(";", () -> { heap[dp++] = EXIT; mode = Mode.INTERPRET; });
  }

  public Forth(int heapSize) {
    this.heap = new Object[heapSize];
    initPrimitives();
    immediate.addAll(Arrays.asList(";", "immediate"));
  }

  public void eval(String source) {
    tokenizer = new StringTokenizer(source);
    while (tokenizer.hasMoreTokens()) {
      String name = next();
      Word word = dict.get(name);
      switch (mode) {
        case INTERPRET:
          if (word != null)
            word.enter();
          else
            stack.push(Integer.parseInt(name));
          break;
        case COMPILE:
          if (word != null) {
            if (immediate.contains(name)) {
              word.enter();
            } else
              heap[dp++] = word;
          } else {
            heap[dp++] = dict.get("lit");
            heap[dp++] = Integer.parseInt(name);
          }
          break;
      }
    }
  }

  private void innerLoop(int address) {
    ip = address;
    Word word = (Word) heap[ip++];
    while (EXIT != word) {
      word.enter();
      word = (Word) heap[ip++];
    }
  }

  private String next() {
    return tokenizer.nextToken();
  }

  public Object pop() {
    return stack.pop();
  }

  public int stackSize() {
    return stack.size();
  }

  public Stack<Object> stack() {
    return stack;
  }
}