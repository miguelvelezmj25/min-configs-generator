package edu.cmu.cs.mvelezce;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureExprParser;
import de.fosd.typechef.featureexpr.FeatureExprParserJava;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.sat.SATFeatureModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConverters;

/** Reimplementation and repurposing of https://github.com/ckaestne/OptimalCoverage. */
public class MinConfigsGenerator {

  private static final FeatureExprParser PARSER =
      new FeatureExprParserJava(FeatureExprFactory.sat());
  //  private static final FeatureExprParser PARSER = new
  // FeatureExprParserJava(FeatureExprFactory.bdd());
  private static final FeatureModel FM = SATFeatureModel.empty();
  //  private static final FeatureModel FM = BDDFeatureModel.empty();

  static {
    FeatureExprFactory.setDefault(FeatureExprFactory.sat());
    //        FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
  }

  /**
   * Generate the minimum number of configurations to satisfy a set of constraints
   *
   * @param options the options of the program.
   * @param constraints the partial configurations.
   * @return
   */
  public static Set<Set<String>> getConfigs(Set<String> options, List<String> constraints) {
    // TODO remove redundancies
    // TODO remove implications

    List<FeatureExpr> featureExprs = parseFeatureExprs(constraints);
    Set<Pair<Integer, Integer>> mutuallyExclusiveFeatureExprs =
        calculateMutualExclusions(featureExprs);
    Collection<SingleFeatureExpr> coloring =
        getColoring(constraints, mutuallyExclusiveFeatureExprs);

    Set<String> colors = getColors(coloring);
    Set<Set<SingleFeatureExpr>> groupingByColors = groupColoringByColors(coloring, colors);
    Set<Set<Integer>> constraintIndexesByColors = getConstraintIndexesByColors(groupingByColors);
    Set<Set<FeatureExpr>> featureExprsByColor =
        getFeatureExprsByColor(featureExprs, constraintIndexesByColors);

    Set<SingleFeatureExpr> singleFeatureExprs = parseSingleFeatureExprs(options);
    scala.collection.immutable.Set<SingleFeatureExpr> singleFeatureExprScalaSet =
        JavaConverters.asScalaSet(singleFeatureExprs).toSet();

    return getConfigs(featureExprsByColor, singleFeatureExprScalaSet);
  }

  private static Set<Set<String>> getConfigs(
      Set<Set<FeatureExpr>> featureExprsByColor,
      scala.collection.immutable.Set<SingleFeatureExpr> singleFeatureExprScalaSet) {
    Set<Set<String>> configs = new HashSet<>();

    for (Set<FeatureExpr> featureExprs : featureExprsByColor) {
      FeatureExpr formula = FeatureExprFactory.True();

      for (FeatureExpr featureExpr : featureExprs) {
        formula = formula.and(featureExpr);
      }

      if (!formula.isSatisfiable()) {
        throw new RuntimeException("The following formula is not satisfiable\n" + formula);
      }

      Option<
              Tuple2<
                  scala.collection.immutable.List<SingleFeatureExpr>,
                  scala.collection.immutable.List<SingleFeatureExpr>>>
          assign = formula.getSatisfiableAssignment(FM, singleFeatureExprScalaSet, true);

      Set<SingleFeatureExpr> singleFeatureExprConfig =
          new HashSet<>(JavaConverters.asJavaCollection(assign.get()._1));

      Set<String> config = new HashSet<>();

      for (SingleFeatureExpr singleFeatureExpr : singleFeatureExprConfig) {
        config.add(singleFeatureExpr.feature());
      }

      configs.add(config);
    }

    return configs;
  }

  private static Set<Set<FeatureExpr>> getFeatureExprsByColor(
      List<FeatureExpr> featureExprs, Set<Set<Integer>> constraintIndexesByColors) {
    Set<Set<FeatureExpr>> featureExprsByColor = new HashSet<>();

    for (Set<Integer> constraintIndexes : constraintIndexesByColors) {
      Set<FeatureExpr> featureExprsGroupedByColor = new HashSet<>();

      for (Integer i : constraintIndexes) {
        FeatureExpr featureExpr = featureExprs.get(i);
        featureExprsGroupedByColor.add(featureExpr);
      }

      featureExprsByColor.add(featureExprsGroupedByColor);
    }

    return featureExprsByColor;
  }

  private static Set<Set<Integer>> getConstraintIndexesByColors(
      Set<Set<SingleFeatureExpr>> groupingByColors) {
    Set<Set<Integer>> constraintIndexesByColors = new HashSet<>();

    for (Set<SingleFeatureExpr> colorGrouping : groupingByColors) {
      Set<Integer> constraintIndexes = getConstraintIndexes(colorGrouping);
      constraintIndexesByColors.add(constraintIndexes);
    }

    return constraintIndexesByColors;
  }

  private static Set<Integer> getConstraintIndexes(Set<SingleFeatureExpr> colorGrouping) {
    Set<Integer> constraintIndexes = new HashSet<>();

    for (SingleFeatureExpr singleFeatureExpr : colorGrouping) {
      String feature = singleFeatureExpr.feature();
      int vIndex = feature.indexOf("v");
      int colorIndex = feature.lastIndexOf("_");
      String constraintIndex = feature.substring(vIndex + 1, colorIndex);

      constraintIndexes.add(Integer.valueOf(constraintIndex));
    }

    return constraintIndexes;
  }

  private static Set<Set<SingleFeatureExpr>> groupColoringByColors(
      Collection<SingleFeatureExpr> coloring, Set<String> colors) {
    Set<Set<SingleFeatureExpr>> groupedColoring = new HashSet<>();

    for (String color : colors) {
      Set<SingleFeatureExpr> colorGroup = new HashSet<>();

      for (SingleFeatureExpr singleFeatureExpr : coloring) {
        if (singleFeatureExpr.feature().endsWith("_" + color)) {
          colorGroup.add(singleFeatureExpr);
        }
      }

      // TODO remove all the feature expressions that we have identified the color from the coloring

      groupedColoring.add(colorGroup);
    }

    return groupedColoring;
  }

  private static Set<String> getColors(Collection<SingleFeatureExpr> coloring) {
    Set<String> colors = new HashSet<>();

    for (SingleFeatureExpr featureExpr : coloring) {
      String feature = featureExpr.feature();
      int colorIndex = feature.lastIndexOf("_");
      String color = feature.substring(colorIndex + 1);
      colors.add(color);
    }

    return colors;
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

      if (!formula.isSatisfiable()) {
        colors++;

        continue;
      }

      scala.collection.immutable.Set<SingleFeatureExpr> scalaInterestingFeatures =
          JavaConverters.asScalaSet(interestingFeatures).toSet();

      Option<
              Tuple2<
                  scala.collection.immutable.List<SingleFeatureExpr>,
                  scala.collection.immutable.List<SingleFeatureExpr>>>
          assign = formula.getSatisfiableAssignment(FM, scalaInterestingFeatures, true);

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
      Set<Pair<Integer, Integer>> mutuallyExclusiveFeatureExprs, int colors) {
    FeatureExpr mutuallyExclusiveConstraints = FeatureExprFactory.True();

    for (int color = 0; color <= colors; color++) {
      FeatureExpr mutuallyExclusiveConstraint = FeatureExprFactory.True();

      for (Pair<Integer, Integer> mutuallyExclusiveExprs : mutuallyExclusiveFeatureExprs) {
        FeatureExpr coloredVertex1 =
            PARSER.parse(createVertex(mutuallyExclusiveExprs.getLeft(), color));
        FeatureExpr coloredVertex2 =
            PARSER.parse(createVertex(mutuallyExclusiveExprs.getRight(), color));

        FeatureExpr mutuallyExclusiveFeatureExpr = coloredVertex1.and(coloredVertex2).not();
        mutuallyExclusiveConstraint = mutuallyExclusiveConstraint.and(mutuallyExclusiveFeatureExpr);
      }

      mutuallyExclusiveConstraints = mutuallyExclusiveConstraints.and(mutuallyExclusiveConstraint);
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

      if (featureExpr.isTautology()) {
        featureExprs.add(FeatureExprFactory.True());
      } else {

        featureExprs.add(featureExpr);
      }
    }

    return featureExprs;
  }

  private static Set<SingleFeatureExpr> parseSingleFeatureExprs(Set<String> options) {
    Set<SingleFeatureExpr> singleFeatureExprs = new HashSet<>();

    for (String option : options) {
      SingleFeatureExpr featureExpr = (SingleFeatureExpr) PARSER.parse(option);
      singleFeatureExprs.add(featureExpr);
    }

    return singleFeatureExprs;
  }
}
