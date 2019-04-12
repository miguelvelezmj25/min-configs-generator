package edu.cmu.cs.mvelezce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class MinConfigsGeneratorTest {

  @Test
  public void getSatConfigs_forExampleShowingNeedForCheckingUnsatConfigCombos() {
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
  public void getSatConfigs0() {
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
  public void getSatConfigs1() {
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
  public void getSatConfigs2() {
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
  public void getSatConfigs3() {
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
  public void getSatConfigs4() {
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
  public void getSatConfigs5() {
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
  public void getSatConfigs6() {
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
  public void getSatConfigs7() {
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
  public void getSatConfigs8() {
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
  public void getSatConfigsSubtrace() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();

    constraints.add("(!A && !B && !C) || (A && !B && !C) || (!A && !B && C) || (A && !B && C)"); // gamma

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
  public void getSatConfigsAndContext() {
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
  public void getSatConfigsRunningExample() {
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

  @Test
  public void getSatConfigs_forTestingEncoding() {
    /*
      x = 0
      if(A)
        x = 1
      if(B)
        x = 2
      if(x > 0)
        ...
    */

    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("B");
    constraints.add("!B");

    constraints.add("!A && !B");
    constraints.add("A");
    constraints.add("B");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(2, satConfigs.iterator().next().size());
  }

  @Test
  public void getSatConfigs_forTestingEncoding2() {
    /*
      if(A)
        if(B)
    */

    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("A && B");
    constraints.add("A && !B");
    constraints.add("(!A && !B) || (!A && B)");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(3, satConfigs.iterator().next().size());
  }

  @Test
  public void getSatConfigs_forTestingEncoding3() {
    /*
      x = 0
      if(A)
        x = 1
      if(B)
        x = 2
      while(x > 0)
        x--
    */

    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("!A");
    constraints.add("!A");

    constraints.add("B");
    constraints.add("!B");

    constraints.add("!A && !B");
    constraints.add("(!A && B) || (A && !B) || (A && B)");

    constraints.add("!A && !B");
    constraints.add("(!A && B) || (A && B)");
    constraints.add("A && !B");

    constraints.add("(!A && !B) || (A && !B)");
    constraints.add("(!A && B) || (A && B)");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(3, satConfigs.iterator().next().size());
  }

  @Test
  public void getSatConfigs_forTesting() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("B");
    constraints.add("!B");

    constraints.add("A && C");
    constraints.add("A && !C");

    constraints.add("B && C");
    constraints.add("B && !C");

    Set<Set<Set<String>>> satConfigs = MinConfigsGenerator.getSatConfigs(options, constraints);

    for (Set<Set<String>> configs : satConfigs) {
      System.out.println(configs);
    }

    Assert.assertEquals(2, satConfigs.iterator().next().size());
  }

}
