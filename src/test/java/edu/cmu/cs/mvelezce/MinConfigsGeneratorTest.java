package edu.cmu.cs.mvelezce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class MinConfigsGeneratorTest {

  @Test
  public void entry() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();
    //    taintConstraints.add("A");
    //    taintConstraints.add("!A");
    //    taintConstraints.add("A & C");
    //    taintConstraints.add("A & !C");
    //    taintConstraints.add("B");
    //    taintConstraints.add("!B");

    //    taintConstraints.add("A");
    //    taintConstraints.add("!A");
    //    taintConstraints.add("B");
    //    taintConstraints.add("!B");
    //    taintConstraints.add("A & C");
    //    taintConstraints.add("A & !C");
    //    taintConstraints.add("B & C");
    //    taintConstraints.add("B & !C");

    //    taintConstraints.add("A");
    //    taintConstraints.add("!A");
    //    taintConstraints.add("B");
    //    taintConstraints.add("!B");
    //    taintConstraints.add("A & !B");
    //    taintConstraints.add("!A & !B");
    //    taintConstraints.add("!A & B");
    //    taintConstraints.add("!A & !B");
    //    taintConstraints.add("!(A | B)");


    MinConfigsGenerator.entry(options, constraints);
  }

  @Test
  public void constraints1() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();
    constraints.add("(!A && !B) || (!A && B)");
    constraints.add("(A && !B) || (A && B)");
    constraints.add("(!A && !B) || (A && !B)");
    constraints.add("(!A && B)  || (A && B)");
    constraints.add("(!A && !B)");
    constraints.add("(!A && B) || (A && !B) || (A && B)");

    Set<Set<String>> configs = MinConfigsGenerator.entry(options, constraints);
    System.out.println(configs);
  }

  @Test
  public void constraints2() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("A && B");

    Set<Set<String>> configs = MinConfigsGenerator.entry(options, constraints);
    System.out.println(configs);
  }

  @Test
  public void constraints3() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();
    constraints.add("(!A && !B) || (!A && B) || (A && !B) || (A && B)");

    constraints.add("(!A && !B) || (!A && B)");
    constraints.add("(A && !B) || (A && B)");

    constraints.add("(!A && !B) || (A && !B)");
    constraints.add("(!A && B)  || (A && B)");

    constraints.add("(!A && !B)");
    constraints.add("(!A && B) || (A && !B) || (A && B)");

    constraints.add("(!A && !B)");
    constraints.add("(!A && B) || (A && !B)");
    constraints.add("(A && B)");

    Set<Set<String>> configs = MinConfigsGenerator.entry(options, constraints);
    System.out.println(configs);
  }
}
