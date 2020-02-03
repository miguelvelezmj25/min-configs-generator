package edu.cmu.cs.mvelezce;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.sat.CNFHelper;
import de.fosd.typechef.featureexpr.sat.SATFeatureExprFactory;
import org.junit.Assert;
import org.junit.Test;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MinConfigsGeneratorTest {

  @Test
  public void getSatConfigs_forExampleShowingNeedForCheckingUnsatConfigCombos() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("A"); // 0
    constraints.add("A || B"); // 0
    constraints.add("B"); // 1
    constraints.add("(!A && B) || (A && !B)"); // 2

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(2, satConfig.size());
  }

  @Test
  public void parseAsFeatureExpr() {
    String a = "(!DUPLICATES && !ENV_IS_LOCKING && !MAX_MEMORY && !CACHE_MODE)";
    String b =
        "((!MAX_MEMORY && !ENV_IS_LOCKING && CACHE_MODE) || (DUPLICATES && !ENV_IS_LOCKING && !MAX_MEMORY && !CACHE_MODE))";
    String c = "(MAX_MEMORY || ENV_IS_LOCKING)";
    String d = "!M && E && (M || E) && (D || E || M)";

    FeatureExpr A = MinConfigsGenerator.parseAsBDDFeatureExpr(a);
    FeatureExpr B = MinConfigsGenerator.parseAsBDDFeatureExpr(b);
    FeatureExpr C = MinConfigsGenerator.parseAsBDDFeatureExpr(c);
    FeatureExpr D = MinConfigsGenerator.parseAsBDDFeatureExpr(d);
    FeatureExpr T = SATFeatureExprFactory.True();
    FeatureExpr F = SATFeatureExprFactory.False();

    Set<String> config = new HashSet<>();
    config.add("A");
    config.add("C");
    config.add("D");
    config.add("E");
    boolean x = A.evaluate(JavaConverters.asScalaSet(config).toSet());
    System.out.println(A.equiv(C));
    System.out.println(A.equivalentTo(C));
    //    String a = "A";
    //    String x = "A";
    ////    String a = "((JECACHESIZE && ((SHAREDCACHE && REPLICATED && !SEQUENTIAL && DUPLICATES)
    // || (!SHAREDCACHE && REPLICATED && !SEQUENTIAL && !DUPLICATES))) || (DUPLICATES &&
    // ((SHAREDCACHE && !REPLICATED && SEQUENTIAL) || (SHAREDCACHE && REPLICATED && !SEQUENTIAL)) &&
    // !JECACHESIZE))";
    ////
    ////    String x = "(SHAREDCACHE && !REPLICATED && !SEQUENTIAL && !JECACHESIZE)";
    ////    String y = "(!DUPLICATES && SHAREDCACHE && !REPLICATED && SEQUENTIAL && !JECACHESIZE)";
    ////    String z = "((DUPLICATES && !REPLICATED && !SHAREDCACHE && !SEQUENTIAL && JECACHESIZE)
    // || (SHAREDCACHE && DUPLICATES && REPLICATED && !JECACHESIZE && !SEQUENTIAL))";
    //
    //    FeatureExpr A = MinConfigsGenerator.parseAsFeatureExpr(a);
    //    FeatureExpr X = MinConfigsGenerator.parseAsFeatureExpr(x);
    ////    FeatureExpr Y = MinConfigsGenerator.parseAsFeatureExpr(y);
    ////    FeatureExpr Z = MinConfigsGenerator.parseAsFeatureExpr(z);
    //
    //    System.out.println(X.implies(A).isTautology());
    //    System.out.println(X.equivalentTo(A));
    ////    System.out.println(Y.implies(A).isTautology());
    ////    System.out.println(Z.implies(A).isTautology());
    //    System.out.println();
  }

  @Test
  public void getSatConfigsX() {
    Set<String> options = new HashSet<>();
    options.add("DUPLICATES");
    options.add("SEQUENTIAL");
    options.add("JECACHESIZE");
    options.add("SHAREDCACHE");
    options.add("REPLICATED");

    List<String> constraints = new ArrayList<>();
    constraints.add(
        "(!JECACHESIZE && !SHAREDCACHE && !DUPLICATES && !REPLICATED && !SEQUENTIAL) || (JECACHESIZE && SHAREDCACHE && !DUPLICATES && REPLICATED && !SEQUENTIAL) || (JECACHESIZE && !SHAREDCACHE && !DUPLICATES && !REPLICATED && !SEQUENTIAL) || (JECACHESIZE && SHAREDCACHE && !DUPLICATES && !REPLICATED && !SEQUENTIAL) || (!JECACHESIZE && SHAREDCACHE && !DUPLICATES && !REPLICATED && !SEQUENTIAL) || (!JECACHESIZE && !SHAREDCACHE && !DUPLICATES && REPLICATED && !SEQUENTIAL) || (JECACHESIZE && !SHAREDCACHE && !DUPLICATES && REPLICATED && !SEQUENTIAL) || (!JECACHESIZE && SHAREDCACHE && !DUPLICATES && REPLICATED && !SEQUENTIAL)");

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    //    Assert.assertEquals(2, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(2, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(2, satConfig.size());
  }

  @Test
  public void getSatConfigs2() {
    Set<String> options = new HashSet<>();
    options.add("A");
    //    options.add("B");

    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(2, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
  }

  @Test
  public void getSatConfigsSubtrace() {
    Set<String> options = new HashSet<>();
    options.add("A");
    options.add("B");
    options.add("C");

    List<String> constraints = new ArrayList<>();

    constraints.add(
        "(!A && !B && !C) || (A && !B && !C) || (!A && !B && C) || (A && !B && C)"); // gamma

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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(4, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(2, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
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

    Set<Set<String>> satConfig = MinConfigsGenerator.getSatConfigs(options, constraints);
    System.out.println(satConfig);

    Assert.assertEquals(3, satConfig.size());
  }
}
