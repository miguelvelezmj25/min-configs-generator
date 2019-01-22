package edu.cmu.cs.mvelezce;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureExprParser;
import de.fosd.typechef.featureexpr.FeatureExprParserJava;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConverters;

public class MinConfigs {

  private static final FeatureExprParser PARSER =
      new FeatureExprParserJava(FeatureExprFactory.sat());

  public static void main(String[] args) {
    List<String> taintConstraints = new ArrayList<>();
    taintConstraints.add("A");
    taintConstraints.add("!A");
    taintConstraints.add("A & C");
    taintConstraints.add("A & !C");
    taintConstraints.add("B");
    taintConstraints.add("!B");

    // TODO remove redundancies

    List<FeatureExpr> featureExprs = parseFeatureExprs(taintConstraints);
    Set<Pair<Integer, Integer>> mutuallyExclusiveFeatureExprs =
        calculateMutualExclusions(featureExprs);
    Collection<SingleFeatureExpr> coloring =
        getColoring(taintConstraints, mutuallyExclusiveFeatureExprs);

    System.out.println(coloring);

    // TODO add in other methods

    Set<String> colors = new HashSet<>();

    for (SingleFeatureExpr featureExpr : coloring) {
      String feature = featureExpr.feature();
      int colorIndex = feature.lastIndexOf("_");
      String color = feature.substring(colorIndex + 1);
      colors.add(color);
    }

    Set<Set<Integer>> constraints = new HashSet<>();

    for (String color : colors) {
      Set<Integer> constraintIndexes = new HashSet<>();

      for (SingleFeatureExpr featureExpr : coloring) {
        String feature = featureExpr.feature();

        if (!feature.endsWith("_" + color)) {
          continue;
        }

        int colorIndex = feature.lastIndexOf("_");
        int constraintIndex = Integer.valueOf(feature.substring(1, colorIndex));

        constraintIndexes.add(constraintIndex);
      }

      constraints.add(constraintIndexes);
    }

    Set<Set<String>> configs = new HashSet<>();

    for (Set<Integer> x : constraints) {
      Set<String> config = new HashSet<>();

      for (Integer i : x) {
        config.add(taintConstraints.get(i));
      }

      configs.add(config);
    }

    for (Set<String> config : configs) {
      System.out.println(config);
    }
  }

  private static Collection<SingleFeatureExpr> getColoring(
      List<String> taintConstraints, Set<Pair<Integer, Integer>> mutuallyExclusiveFeatureExprs) {
    Set<SingleFeatureExpr> interestingFeatures = new HashSet<>();

    int colors = 0;

    while (true) {
      interestingFeatures = addInterestingFeatures(interestingFeatures, taintConstraints, colors);

      FeatureExpr assignedColors = assignColors(taintConstraints, colors);
      FeatureExpr mutuallyExclusiveConstraints =
          getMutuallyExclusiveConstraints(mutuallyExclusiveFeatureExprs, colors);
      FeatureExpr formula = assignedColors.and(mutuallyExclusiveConstraints);

      scala.collection.immutable.Set<SingleFeatureExpr> scalaInterestingFeatures =
          JavaConverters.asScalaSet(interestingFeatures).toSet();

      Option<
              Tuple2<
                  scala.collection.immutable.List<SingleFeatureExpr>,
                  scala.collection.immutable.List<SingleFeatureExpr>>>
          assign = formula.getSatisfiableAssignment(null, scalaInterestingFeatures, true);

      if (assign.isEmpty()) {
        colors++;

        continue;
      }

      return JavaConverters.asJavaCollection(assign.get()._1);
    }
  }

  private static Set<SingleFeatureExpr> addInterestingFeatures(
      Set<SingleFeatureExpr> interestingFeatures, List<String> constraints, int color) {
    Set<SingleFeatureExpr> newFeatures = new HashSet<>();

    for (int i = 0; i < constraints.size(); i++) {
      SingleFeatureExpr newFeature = (SingleFeatureExpr) PARSER.parse(createVertex(i, color));
      newFeatures.add(newFeature);
    }

    interestingFeatures.addAll(newFeatures);

    return interestingFeatures;
  }

  private static FeatureExpr getMutuallyExclusiveConstraints(
      Set<Pair<Integer, Integer>> mex, int colors) {
    FeatureExpr mutuallyExclusiveConstraints = FeatureExprFactory.True();

    for (int color = 0; color <= colors; color++) {
      FeatureExpr something = FeatureExprFactory.True();

      for (Pair<Integer, Integer> m : mex) {
        FeatureExpr coloredVertex1 = PARSER.parse(createVertex(m.getLeft(), color));
        FeatureExpr coloredVertex2 = PARSER.parse(createVertex(m.getRight(), color));

        FeatureExpr con = coloredVertex1.and(coloredVertex2).not();
        something = something.and(con);
      }

      mutuallyExclusiveConstraints = mutuallyExclusiveConstraints.and(something);
    }

    return mutuallyExclusiveConstraints;
  }

  private static String createVertex(int vertex, int color) {
    return "v" + vertex + "_" + color;
  }

  private static FeatureExpr assignColors(List<String> constraints, int colors) {
    FeatureExpr assignedColors = FeatureExprFactory.True();

    for (int vertex = 0; vertex < constraints.size(); vertex++) {
      FeatureExpr vertices = FeatureExprFactory.False();

      for (int color = 0; color <= colors; color++) {
        FeatureExpr coloredVertex = PARSER.parse(createVertex(vertex, color));
        vertices = vertices.or(coloredVertex);
      }

      assignedColors = assignedColors.and(vertices);
    }

    return assignedColors;
  }

  private static Map<FeatureExpr, FeatureExpr> encodeUUIDs(Map<FeatureExpr, FeatureExpr> map1) {
    Map<FeatureExpr, FeatureExpr> map = new HashMap<>();

    for (Map.Entry<FeatureExpr, FeatureExpr> entry : map1.entrySet()) {
      map.put(entry.getValue(), entry.getKey());
    }

    return map;
  }

  private static Map<FeatureExpr, FeatureExpr> encodeFeatureExpr(Set<FeatureExpr> featureExprs) {
    FeatureExprParser parser = new FeatureExprParserJava(FeatureExprFactory.sat());
    Map<FeatureExpr, FeatureExpr> map = new HashMap<>();

    for (FeatureExpr featureExpr : featureExprs) {
      UUID uuid = UUID.randomUUID();
      String s = uuid.toString();
      int dashIndex = s.indexOf("-");
      s = s.substring(0, dashIndex);

      map.put(featureExpr, parser.parse(s));
    }

    return map;
  }

  private static Set<Pair<Integer, Integer>> calculateMutualExclusions(
      List<FeatureExpr> featureExprs) {
    Set<Pair<Integer, Integer>> mutuallyExclusiveFeatureExprs = new HashSet<>();

    for (int i = 0; i < (featureExprs.size() - 1); i++) {
      FeatureExpr featureExpr1 = featureExprs.get(i);

      for (int j = (i + 1); j < featureExprs.size(); j++) {
        FeatureExpr featureExpr2 = featureExprs.get(j);

        if (featureExpr1.mex(featureExpr2).isTautology()) {
          Pair<Integer, Integer> pair = Pair.of(i, j);
          mutuallyExclusiveFeatureExprs.add(pair);
        }
      }
    }

    return mutuallyExclusiveFeatureExprs;
  }

  private static List<FeatureExpr> parseFeatureExprs(List<String> taintConstraints) {
    List<FeatureExpr> featureExprs = new ArrayList<>();

    for (String constraint : taintConstraints) {
      FeatureExpr featureExpr = PARSER.parse(constraint);
      featureExprs.add(featureExpr);
    }

    return featureExprs;
  }
}
