package edu.cmu.cs.mvelezce;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureExprParser;
import de.fosd.typechef.featureexpr.FeatureExprParserJava;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.sat.SATFeatureModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Combinations;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConverters;

/** Reimplementation and repurposing of https://github.com/ckaestne/OptimalCoverage. */
public class MinConfigsGenerator {

  private static final FeatureExprParser SAT_PARSER =
      new FeatureExprParserJava(FeatureExprFactory.sat());
  private static final FeatureExprParser BDD_PARSER =
      new FeatureExprParserJava(FeatureExprFactory.bdd());
  private static final FeatureModel SAT_FM = SATFeatureModel.empty();
  //  private static final FeatureModel FM = BDDFeatureModel.empty();

  /**
   * Generate the minimum number of configurations to satisfy a set of constraints
   *
   * @param options the options of the program.
   * @param constraints the partial configurations.
   * @return
   */
  public static Set<Set<String>> getConfigs(Set<String> options, List<String> constraints) {
    List<FeatureExpr> featureExprs = parseFeatureExprsAsBDD(constraints);
    featureExprs = removeRedundant(featureExprs);
    featureExprs = toSatFeatureExprs(featureExprs);

    Set<Pair<Integer, Integer>> mutuallyExclusiveVertices =
        calculateVertexMutualExclusions(featureExprs);

    Set<Set<FeatureExpr>> featureExprsCombos = getFeatureExprsCombos(featureExprs);
    Set<Set<Integer>> unsatVertexCombos =
        calculateUnsatVertexCombos(featureExprs, featureExprsCombos);
    unsatVertexCombos =
        removeUnsatVertexCombosCoveredByMex(unsatVertexCombos, mutuallyExclusiveVertices);

    Collection<SingleFeatureExpr> coloring =
        getColoring(featureExprs, mutuallyExclusiveVertices, unsatVertexCombos);

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

  private static Set<Set<Integer>> removeUnsatVertexCombosCoveredByMex(
      Set<Set<Integer>> unsatVertexCombos, Set<Pair<Integer, Integer>> mutuallyExclusiveVertices) {
    Set<Set<Integer>> combosToRemove = new HashSet<>();

    for (Pair<Integer, Integer> mexVertices : mutuallyExclusiveVertices) {
      Set<Integer> vertices = new HashSet<>();
      vertices.add(mexVertices.getLeft());
      vertices.add(mexVertices.getRight());

      for (Set<Integer> unsat : unsatVertexCombos) {
        if (unsat.containsAll(vertices)) {
          combosToRemove.add(unsat);
        }
      }
    }

    Set<Set<Integer>> res = new HashSet<>(unsatVertexCombos);
    res.removeAll(combosToRemove);

    return res;
  }

  private static Set<Set<Integer>> calculateUnsatVertexCombos(
      List<FeatureExpr> featureExprs, Set<Set<FeatureExpr>> featureExprsCombos) {
    Set<Set<Integer>> unsatVertexCombos = new HashSet<>();

    for (Set<FeatureExpr> featureExprsInCombo : featureExprsCombos) {
      if (isSatCombo(featureExprsInCombo)) {
        continue;
      }

      // TODO might be slow
      Set<Integer> unsatVertexCombo = new HashSet<>();

      for (FeatureExpr featureExpr : featureExprsInCombo) {
        int index = featureExprs.indexOf(featureExpr);
        unsatVertexCombo.add(index);
      }

      unsatVertexCombos.add(unsatVertexCombo);
    }

    return unsatVertexCombos;
  }

  private static boolean isSatCombo(Set<FeatureExpr> featureExprs) {
    FeatureExpr formula = FeatureExprFactory.True();

    for (FeatureExpr featureExpr : featureExprs) {
      formula = formula.and(featureExpr);
    }

    return formula.isSatisfiable();
  }

  private static Set<Set<FeatureExpr>> getFeatureExprsCombos(List<FeatureExpr> featureExprs) {
    Set<Set<FeatureExpr>> combos = new HashSet<>();
    int comboMaxLength = featureExprs.size();

    for (int i = 2; i <= comboMaxLength; i++) {
      Combinations currentCombos = new Combinations(comboMaxLength, i);

      for (int[] currentCombo : currentCombos) {
        Set<FeatureExpr> combo = new HashSet<>();

        for (int element : currentCombo) {
          combo.add(featureExprs.get(element));
        }

        combos.add(combo);
      }
    }

    return combos;
  }

  private static List<FeatureExpr> toSatFeatureExprs(List<FeatureExpr> featureExprs) {
    List<FeatureExpr> satFeatureExprs = new ArrayList<>();

    for (FeatureExpr featureExpr : featureExprs) {
      FeatureExpr satFeatureExpr = ((BDDFeatureExpr) featureExpr).toSATFeatureExpr();
      satFeatureExprs.add(satFeatureExpr);
    }

    FeatureExprFactory.setDefault(FeatureExprFactory.sat());

    return satFeatureExprs;
  }

  private static List<FeatureExpr> removeRedundant(List<FeatureExpr> featureExprs) {
    Set<FeatureExpr> uniqueFeatureExprsSet = new HashSet<>();
    List<FeatureExpr> uniqueFeatureExprs = new ArrayList<>();

    for (FeatureExpr featureExpr : featureExprs) {
      if (uniqueFeatureExprsSet.contains(featureExpr)) {
        continue;
      }

      uniqueFeatureExprs.add(featureExpr);
      uniqueFeatureExprsSet.add(featureExpr);
    }

    return uniqueFeatureExprs;
  }

  //  private static List<FeatureExpr> removeImplications(List<FeatureExpr> featureExprs) {
  //    List<FeatureExpr> implFormula = new ArrayList<>();
  //
  //    for (FeatureExpr featureExpr1 : featureExprs) {
  //      boolean some = true;
  //
  //      for (FeatureExpr featureExpr2 : implFormula) {
  //        if (featureExpr2.implies(featureExpr1).isTautology()) {
  //          some = false;
  //          break;
  //        }
  //      }
  //
  //      if (some) {
  //        implFormula.add(featureExpr1);
  //      }
  //    }

  //    Set<FeatureExpr> implied = new HashSet<>();
  //
  //    for (int i = 0; i < (featureExprs.size() - 1); i++) {
  //      FeatureExpr featureExpr1 = featureExprs.get(i);
  //
  //      for (int j = 0; j < featureExprs.size(); j++) {
  //        FeatureExpr featureExpr2 = featureExprs.get(j);
  //
  //        if(i == j) {
  //          continue;
  //        }
  //
  //        if (featureExpr1.implies(featureExpr2).isTautology()) {
  //          implied.add(featureExpr2);
  //        }
  //      }
  //    }
  //
  //    List<FeatureExpr> x = new ArrayList<>();
  //
  //    for(FeatureExpr featureExpr : featureExprs) {
  //      if(implied.contains(featureExpr)) {
  //        continue;
  //      }
  //
  //      x.add(featureExpr);
  //    }
  //
  //    return x;

  //    throw new UnsupportedOperationException("Implement");
  //  }

  private static Set<FeatureExpr> getCounterExample(Set<Set<FeatureExpr>> featureExprsByColor) {
    for (Set<FeatureExpr> featureExprs : featureExprsByColor) {
      FeatureExpr formula = FeatureExprFactory.True();

      for (FeatureExpr featureExpr : featureExprs) {
        formula = formula.and(featureExpr);
      }

      if (!formula.isSatisfiable()) {
        return featureExprs;
      }
    }

    return new HashSet<>();
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
        throw new RuntimeException("The following formula is not satisfiable\n" + featureExprs);
      }

      Option<
              Tuple2<
                  scala.collection.immutable.List<SingleFeatureExpr>,
                  scala.collection.immutable.List<SingleFeatureExpr>>>
          assign = formula.getSatisfiableAssignment(SAT_FM, singleFeatureExprScalaSet, true);

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

      // TODO removeUnsatVertexCombosCoveredByMex all the feature expressions that we have
      // identified the color from the coloring

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
      List<FeatureExpr> featureExprs,
      Set<Pair<Integer, Integer>> mutuallyExclusiveVertices,
      Set<Set<Integer>> unsatVertexCombos) {

    int colors = 0;

    while (true) {
      Set<SingleFeatureExpr> interestingFeatures = addInterestingFeatures(featureExprs, colors);

      FeatureExpr assignedColors = assignColors(featureExprs, colors);
      FeatureExpr mutuallyExclusiveConstraints =
          getMutuallyExclusiveConstraints(mutuallyExclusiveVertices, colors);
      FeatureExpr unsatCombosConstraints = getUnsatCombos(unsatVertexCombos, colors);
      FeatureExpr formula = assignedColors.and(mutuallyExclusiveConstraints);
      formula = formula.and(unsatCombosConstraints);

      if (!formula.isSatisfiable()) {
        colors++;

        continue;
      }

      return getColoringAssign(formula, interestingFeatures);
    }
  }

  private static FeatureExpr getUnsatCombos(Set<Set<Integer>> unsatVertexCombos, int colors) {
    FeatureExpr unsatCombosConstraints = FeatureExprFactory.True();

    for (int color = 0; color <= colors; color++) {
      FeatureExpr unsatCombosConstraint = FeatureExprFactory.True();

      for (Set<Integer> unsatFeatureExprs : unsatVertexCombos) {
        FeatureExpr unSatFeatureExpr = FeatureExprFactory.True();

        for (Integer featureExpr : unsatFeatureExprs) {
          FeatureExpr vertex = SAT_PARSER.parse(createVertex(featureExpr, color));
          unSatFeatureExpr = unSatFeatureExpr.and(vertex);
        }

        unSatFeatureExpr = unSatFeatureExpr.not();
        unsatCombosConstraint = unsatCombosConstraint.and(unSatFeatureExpr);
      }

      unsatCombosConstraints = unsatCombosConstraints.and(unsatCombosConstraint);
    }

    return unsatCombosConstraints;
  }

  private static Collection<SingleFeatureExpr> getColoringAssign(
      FeatureExpr formula, Set<SingleFeatureExpr> interestingFeatures) {
    scala.collection.immutable.Set<SingleFeatureExpr> scalaInterestingFeatures =
        JavaConverters.asScalaSet(interestingFeatures).toSet();

    Option<
            Tuple2<
                scala.collection.immutable.List<SingleFeatureExpr>,
                scala.collection.immutable.List<SingleFeatureExpr>>>
        assign = formula.getSatisfiableAssignment(SAT_FM, scalaInterestingFeatures, true);

    return JavaConverters.asJavaCollection(assign.get()._1);
  }

  //  private static FeatureExpr checkSatConfigAssign(
  //      Collection<SingleFeatureExpr> coloring, List<FeatureExpr> featureExprs, Set<String>
  // options) {
  //    Set<String> colors = getColors(coloring);
  //    Set<Set<SingleFeatureExpr>> groupingByColors = groupColoringByColors(coloring, colors);
  //    Set<Set<Integer>> constraintIndexesByColors =
  // getConstraintIndexesByColors(groupingByColors);
  //    Set<Set<FeatureExpr>> featureExprsByColor =
  //        getFeatureExprsByColor(featureExprs, constraintIndexesByColors);
  //
  //    Set<SingleFeatureExpr> singleFeatureExprs = parseSingleFeatureExprs(options);
  //    scala.collection.immutable.Set<SingleFeatureExpr> singleFeatureExprScalaSet =
  //        JavaConverters.asScalaSet(singleFeatureExprs).toSet();
  //
  //    Set<FeatureExpr> counterExample =
  //        getCounterExample(featureExprsByColor, singleFeatureExprScalaSet);
  //
  //    if (!counterExample.isEmpty()) {
  //      FeatureExpr counterExampleFormula = FeatureExprFactory.True();
  //
  //      for (FeatureExpr featureExpr : counterExample) {
  //        counterExampleFormula.and(featureExpr);
  //      }
  //
  //      return counterExampleFormula.not();
  //    }
  //
  //    return FeatureExprFactory.True();
  //  }

  private static Set<SingleFeatureExpr> addInterestingFeatures(
      List<FeatureExpr> featureExprs, int color) {
    Set<SingleFeatureExpr> newFeatures = new HashSet<>();

    for (int c = 0; c <= color; c++) {
      for (int i = 0; i < featureExprs.size(); i++) {
        SingleFeatureExpr newFeature = (SingleFeatureExpr) SAT_PARSER.parse(createVertex(i, c));
        newFeatures.add(newFeature);
      }
    }

    return newFeatures;
  }

  //  private static Set<SingleFeatureExpr> addInterestingFeatures(
  //      Set<SingleFeatureExpr> interestingFeatures, List<FeatureExpr> featureExprs, int color) {
  //    Set<SingleFeatureExpr> newFeatures = new HashSet<>();
  //
  //    for (int i = 0; i < featureExprs.size(); i++) {
  //      SingleFeatureExpr newFeature = (SingleFeatureExpr) SAT_PARSER.parse(createVertex(i,
  // color));
  //      newFeatures.add(newFeature);
  //    }
  //
  //    interestingFeatures.addAll(newFeatures);
  //
  //    return interestingFeatures;
  //  }

  private static FeatureExpr getMutuallyExclusiveConstraints(
      Set<Pair<Integer, Integer>> mutuallyExclusiveVertices, int colors) {
    FeatureExpr mutuallyExclusiveConstraints = FeatureExprFactory.True();

    for (int color = 0; color <= colors; color++) {
      FeatureExpr mutuallyExclusiveConstraint = FeatureExprFactory.True();

      for (Pair<Integer, Integer> mutuallyExclusiveVertex : mutuallyExclusiveVertices) {
        FeatureExpr coloredVertex1 =
            SAT_PARSER.parse(createVertex(mutuallyExclusiveVertex.getLeft(), color));
        FeatureExpr coloredVertex2 =
            SAT_PARSER.parse(createVertex(mutuallyExclusiveVertex.getRight(), color));

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

  private static FeatureExpr assignColors(List<FeatureExpr> featureExprs, int colors) {
    FeatureExpr assignedColors = FeatureExprFactory.True();

    for (int vertex = 0; vertex < featureExprs.size(); vertex++) {
      FeatureExpr vertices = FeatureExprFactory.False();

      for (int color = 0; color <= colors; color++) {
        FeatureExpr coloredVertex = SAT_PARSER.parse(createVertex(vertex, color));
        vertices = vertices.or(coloredVertex);
      }

      assignedColors = assignedColors.and(vertices);
    }

    return assignedColors;
  }

  private static Set<Pair<Integer, Integer>> calculateVertexMutualExclusions(
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

  private static List<FeatureExpr> parseFeatureExprsAsBDD(List<String> taintConstraints) {
    FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
    List<FeatureExpr> featureExprs = new ArrayList<>();

    for (String constraint : taintConstraints) {
      FeatureExpr featureExpr = BDD_PARSER.parse(constraint);

      if (!featureExpr.isTautology()) {
        //        featureExprs.add(FeatureExprFactory.True());
        //      } else {

        featureExprs.add(featureExpr);
      }
    }

    return featureExprs;
  }

  private static Set<SingleFeatureExpr> parseSingleFeatureExprs(Set<String> options) {
    Set<SingleFeatureExpr> singleFeatureExprs = new HashSet<>();

    for (String option : options) {
      SingleFeatureExpr featureExpr = (SingleFeatureExpr) SAT_PARSER.parse(option);
      singleFeatureExprs.add(featureExpr);
    }

    return singleFeatureExprs;
  }
}
