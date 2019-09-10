package edu.cmu.cs.mvelezce.parser.bdd;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureExprParserJava;
import edu.cmu.cs.mvelezce.parser.FeatureExprParser;
import java.util.ArrayList;
import java.util.List;

// Noninstantiable utility class
public class BDDFeatureExprParser implements FeatureExprParser {

  private static final de.fosd.typechef.featureexpr.FeatureExprParser BDD_PARSER =
      new FeatureExprParserJava(FeatureExprFactory.bdd());

  // Suppress default constructor for noninstantiability
  private BDDFeatureExprParser() {}

  public static FeatureExpr parseFeatureExprAsBDD(String constraint) {
    FeatureExprFactory.setDefault(FeatureExprFactory.bdd());

    return BDD_PARSER.parse(constraint);
  }

  public static List<FeatureExpr> parseFeatureExprsAsBDD(List<String> constraints) {
    FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
    List<FeatureExpr> featureExprs = new ArrayList<>();

    for (String constraint : constraints) {
      FeatureExpr featureExpr = parseFeatureExprAsBDD(constraint);

      if (!featureExpr.isTautology()) {
        featureExprs.add(featureExpr);
      }
    }

    return featureExprs;
  }
}
