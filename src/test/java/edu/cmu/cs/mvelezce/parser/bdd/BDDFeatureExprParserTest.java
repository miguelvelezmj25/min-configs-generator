package edu.cmu.cs.mvelezce.parser.bdd;

import de.fosd.typechef.featureexpr.FeatureExpr;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class BDDFeatureExprParserTest {

  @Test
  public void parseFeatureExprsAsBDD_0() {
    List<String> constraints = new ArrayList<>();
    constraints.add("!A");
    constraints.add("A");
    constraints.add("B");
    constraints.add("!B");

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_1() {
    List<String> constraints = new ArrayList<>();
    constraints.add("(!A && !B) || (!A && B)");
    constraints.add("(A && !B) || (A && B)");
    constraints.add("(!A && !B) || (A && !B)");
    constraints.add("(!A && B)  || (A && B)");
    constraints.add("(!A && !B)");
    constraints.add("(!A && B) || (A && !B) || (A && B)");

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_2() {
    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("A && B");

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_3() {
    List<String> constraints = new ArrayList<>();
    constraints.add("(!A && !B) || (!A && B) || (A && !B) || (A && B)"); // TRUE

    constraints.add("(!A && !B) || (!A && B)");
    constraints.add("(A && !B) || (A && B)");

    constraints.add("(!A && !B) || (A && !B)");
    constraints.add("(!A && B)  || (A && B)");

    constraints.add("(!A && !B)");
    constraints.add("(!A && B) || (A && !B) || (A && B)");

    constraints.add("(!A && !B)");
    constraints.add("(!A && B) || (A && !B)");
    constraints.add("(A && B)");

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size() - 1, bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_4() {
    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("B");
    constraints.add("!B");

    constraints.add("B");
    constraints.add("!B");

    constraints.add("A && C");
    constraints.add("A && !C");

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_5() {
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

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_6() {
    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("A && B");
    constraints.add("A && !B");

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_7() {
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

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_8() {
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

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_9() {
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

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_10() {
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

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_11() {
    List<String> constraints = new ArrayList<>();
    constraints.add("A && B");
    constraints.add("A && !B");
    constraints.add("A");
    constraints.add("!A");

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_12() {
    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("B");
    constraints.add("!B");

    constraints.add("!A && !B");
    constraints.add("A");
    constraints.add("B");

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_13() {
    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("A && B");
    constraints.add("A && !B");
    constraints.add("(!A && !B) || (!A && B)");

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_14() {
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

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }

  @Test
  public void parseFeatureExprsAsBDD_15() {
    List<String> constraints = new ArrayList<>();
    constraints.add("A");
    constraints.add("!A");

    constraints.add("B");
    constraints.add("!B");

    constraints.add("A && C");
    constraints.add("A && !C");

    constraints.add("B && C");
    constraints.add("B && !C");

    List<FeatureExpr> bddsFeatureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
    Assert.assertEquals(constraints.size(), bddsFeatureExprs.size());
  }
}
