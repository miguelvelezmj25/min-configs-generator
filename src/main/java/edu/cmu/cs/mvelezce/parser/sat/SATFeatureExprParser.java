package edu.cmu.cs.mvelezce.parser.sat;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;

import java.util.ArrayList;
import java.util.List;

// Noninstantiable utility class
public class SATFeatureExprParser {

  // Suppress default constructor for noninstantiability
  private SATFeatureExprParser() {}

  public static List<FeatureExpr> BDDFeatureExprstoSatFeatureExprs(List<FeatureExpr> featureExprs) {
    List<FeatureExpr> satFeatureExprs = new ArrayList<>();

    for (FeatureExpr featureExpr : featureExprs) {
      FeatureExpr satFeatureExpr = ((BDDFeatureExpr) featureExpr).toSATFeatureExpr();
      satFeatureExprs.add(satFeatureExpr);
    }

    System.err.println(
        "Setting the featureExpr default to sat here is bad practice since it assumes a pattern to follow");
    FeatureExprFactory.setDefault(FeatureExprFactory.sat());

    return satFeatureExprs;
  }
}
