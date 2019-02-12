package edu.cmu.cs.mvelezce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class MinConfigsGeneratorTest {

  @Test
  public void getConfigs_forExampleShowingNeedForCheckingUnsatConfigCombos() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("!A"); // 0
    constraints.add("A"); // 0
    constraints.add("B"); // 1
    constraints.add("(!A && B) || (A && !B)"); // 2

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(2, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigs0() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");
    constraints.add("B");
    constraints.add("!B");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(2, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigs1() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("(!A && !B) || (!A && B)");
    constraints.add("(A && !B) || (A && B)");
    constraints.add("(!A && !B) || (A && !B)");
    constraints.add("(!A && B)  || (A && B)");
    constraints.add("(!A && !B)");
    constraints.add("(!A && B) || (A && !B) || (A && B)");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(2, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigs2() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("A && B");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(1, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigs3() {
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

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(3, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigs4() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("B");
    constraints.add("!B");

    constraints.add("B");
    constraints.add("!B");

    constraints.add("A && C");
    constraints.add("A && !C");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(3, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigs5() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("B");
    constraints.add("!B");

    // TODO how to encode "A v B"?
    constraints.add("!(A || B)");
    constraints.add("A && !B");
    constraints.add("!A && !B");
    constraints.add("!A && B");
    constraints.add("!A && !B");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(3, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigs6() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("A && B");
    constraints.add("A && !B");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(3, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigs7() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("!A");
    constraints.add("(A && !B && !C) || (A && !B && C)");
    constraints.add("(A && B && !C) || (A && B && C)");

    constraints.add("A");
    constraints.add("!A");

    constraints.add("!A");
    constraints.add("(A && !B && !C) || (A && B && !C)");
    constraints.add("(A && !B && C) || (A && B && C)");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(3, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigs8() {
    /*
    main()
      if(A)
        foo(C);
      if(B)
        foo(C);

    foo(x)
      if(x)
        ...
     */
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("!A");
    constraints.add("(A && !B && !C) || (A && B && !C)");
    constraints.add("(A && !B && C) || (A && B && C)");

    constraints.add("B");
    constraints.add("!B");

    constraints.add("!B");
    constraints.add("(!A && B && !C) || (A && B && !C)");
    constraints.add("(!A && B && C) || (A && B && C)");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(3, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigsSubtrace() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();

    constraints.add("(!A && !B && !C) || (A && !B && !C) || (!A && !B && C) || (A && !B && C)");
    constraints.add("(!A && B && !C) || (A && B && !C)");
    constraints.add("(!A && B && C) || (A && B && C)");
    constraints.add("(!A && !B && !C) || (!A && !B && C)");
    constraints.add(
        "(A && !B && !C) || (!A && B && !C) || (A && B && !C) || (A && !B && C) || (!A && B && C) || (A && B && C)");
    constraints.add("(!A && !B && !C) || (!A && B && !C) || (!A && !B && C) || (!A && B && C)");
    constraints.add("(A && !B && !C) || (A && B && !C) || (A && !B && C) || (A && B && C)");
    constraints.add("(!A && !B && !C) || (A && !B && !C) || (!A && !B && C) || (A && !B && C)");
    constraints.add("(!A && B && !C) || (A && B && !C) || (!A && B && C) || (A && B && C)");
    constraints.add("(!A && !B && !C) || (!A && B && !C) || (!A && !B && C) || (!A && B && C)");
    constraints.add("(A && !B && !C) || (A && B && !C)");
    constraints.add("(A && !B && C) || (A && B && C)");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(3, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigsAndContext() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");
    options.add("D");

    List<String> constraints = new ArrayList<>();
    constraints.add(
        "(!A && !B && !C && !D) || (A && !B && !C && !D) || (!A && B && !C && !D) || (!A && !B && C && !D) || (A && B && !C && !D) || (!A && !B && !C && D) || (!A && B && C && !D) || (A && !B && !C && D) || (!A && B && !C && D) || (!A && !B && C && D) || (A && B && !C && D) || (!A && B && C && D)");
    constraints.add("(A && !B && C && !D) || (A && B && C && !D)");
    constraints.add("(A && !B && C && D) || (A && B && C && D)");
    constraints.add(
        "(!A && !B && !C && !D) || (A && !B && !C && !D) || (!A && B && !C && !D) || (!A && !B && C && !D) || (A && !B && C && !D) || (!A && !B && !C && D) || (!A && B && C && !D) || (A && !B && !C && D) || (!A && B && !C && D) || (!A && !B && C && D) || (A && !B && C && D) || (!A && B && C && D)");
    constraints.add("(A && B && !C && !D) || (A && B && C && !D)");
    constraints.add("(A && B && !C && D) || (A && B && C && D)");
    constraints.add(
        "(!A && !B && !C && !D) || (!A && B && !C && !D) || (!A && !B && C && !D) || (!A && !B && !C && D) || (!A && B && C && !D) || (!A && B && !C && D) || (!A && !B && C && D) || (!A && B && C && D)");
    constraints.add(
        "(A && !B && !C && !D) || (A && B && !C && !D) || (A && !B && C && !D) || (A && !B && !C && D) || (A && B && C && !D) || (A && B && !C && D) || (A && !B && C && D) || (A && B && C && D)");
    constraints.add(
        "(!A && !B && !C && !D) || (!A && B && !C && !D) || (!A && !B && C && !D) || (!A && !B && !C && D) || (!A && B && C && !D) || (!A && B && !C && D) || (!A && !B && C && D) || (!A && B && C && D)");
    constraints.add(
        "(A && !B && !C && !D) || (A && !B && C && !D) || (A && !B && !C && D) || (A && !B && C && D)");
    constraints.add(
        "(A && B && !C && !D) || (A && B && C && !D) || (A && B && !C && D) || (A && B && C && D)");
    constraints.add(
        "(!A && !B && !C && !D) || (!A && B && !C && !D) || (!A && !B && C && !D) || (!A && !B && !C && D) || (!A && B && C && !D) || (!A && B && !C && D) || (!A && !B && C && D) || (!A && B && C && D)");
    constraints.add(
        "(A && !B && !C && !D) || (A && B && !C && !D) || (A && !B && !C && D) || (A && B && !C && D)");
    constraints.add(
        "(A && !B && C && !D) || (A && B && C && !D) || (A && !B && C && D) || (A && B && C && D)");
    constraints.add(
        "(!A && !B && !C && !D) || (!A && B && !C && !D) || (!A && !B && C && !D) || (!A && !B && !C && D) || (!A && B && C && !D) || (!A && B && !C && D) || (!A && !B && C && D) || (!A && B && C && D)");
    constraints.add(
        "(A && !B && !C && !D) || (A && B && !C && !D) || (A && !B && C && !D) || (A && !B && !C && D) || (A && B && C && !D) || (A && B && !C && D) || (A && !B && C && D) || (A && B && C && D)");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(4, satConfigs.iterator().next().size());
  }

  @Test
  public void getConfigsRunningExample() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("A && B");
    constraints.add("A && !B");
    constraints.add("A");
    constraints.add("!A");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(3, satConfigs.iterator().next().size());
  }
}
