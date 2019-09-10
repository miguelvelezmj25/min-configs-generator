package edu.cmu.cs.mvelezce;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.FeatureExprParser;
import de.fosd.typechef.featureexpr.FeatureExprParserJava;
import de.fosd.typechef.featureexpr.FeatureModel;
import de.fosd.typechef.featureexpr.SingleFeatureExpr;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.sat.SATFeatureModel;
import edu.cmu.cs.mvelezce.parser.bdd.BDDFeatureExprParser;
import edu.cmu.cs.mvelezce.parser.sat.SATFeatureExprParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Combinations;
import scala.Option;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.JavaConverters;

/** Reimplementation and repurposing of https://github.com/ckaestne/OptimalCoverage. */
public class MinConfigsGenerator {

  private static final FeatureExprParser SAT_PARSER =
      new FeatureExprParserJava(FeatureExprFactory.sat());
  private static final FeatureModel SAT_FM = SATFeatureModel.empty();
  //    private static final FeatureModel FM = BDDFeatureModel.empty();

  /**
   * Generate the minimum number of configurations to satisfy a set of constraints
   *
   * @param options the options of the program.
   * @param constraints the partial configurations.
   * @return
   */
  public static Set<Set<String>> getSatConfigs(Set<String> options, List<String> constraints) {
    List<FeatureExpr> featureExprs = BDDFeatureExprParser.parseFeatureExprsAsBDD(constraints);
//    featureExprs = removeRedundant(featureExprs);
    featureExprs = SATFeatureExprParser.toSatFeatureExprs(featureExprs);

    Set<Pair<Integer, Integer>> mutexVertices = MinConfigsGenerator.getMutexVertices(featureExprs);

    ////    List<Set<FeatureExpr>> featureExprsCombos = getFeatureExprsCombos(featureExprs);
    //    Set<Set<Integer>> unsatVertexCombos = getUnsatVertexCombos(featureExprs, featureExprs);

    Collection<SingleFeatureExpr> coloring = getColoring(featureExprs, mutexVertices, constraints);
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

  private static Set<Pair<Integer, Integer>> getMutexVertices(List<FeatureExpr> featureExprs) {
    Set<Pair<Integer, Integer>> mutexVertices = new HashSet<>();

    for (int i = 0; i < (featureExprs.size() - 1); i++) {
      FeatureExpr featureExpr1 = featureExprs.get(i);

      for (int j = (i + 1); j < featureExprs.size(); j++) {
        FeatureExpr featureExpr2 = featureExprs.get(j);

        if (featureExpr1.mex(featureExpr2).isTautology()) {
          Pair<Integer, Integer> pair = Pair.of(i, j);
          mutexVertices.add(pair);
        }
      }
    }

    return mutexVertices;
  }

  private static Set<Set<Integer>> getUnsatVertexCombos(
      List<FeatureExpr> featureExprs, List<Set<FeatureExpr>> featureExprsCombos) {
    Set<Set<FeatureExpr>> unsatCombos = getUnsatCombos(featureExprsCombos);

    return getUnSatVertexCombos(featureExprs, unsatCombos);
  }

  // TODO might be slow
  private static Set<Set<Integer>> getUnSatVertexCombos(
      List<FeatureExpr> featureExprs, Set<Set<FeatureExpr>> unsatCombos) {
    Set<Set<Integer>> unsatVertexCombos = new HashSet<>();

    for (Set<FeatureExpr> unsatCombo : unsatCombos) {
      Set<Integer> unsatVertexCombo = new HashSet<>();

      for (FeatureExpr featureExpr : unsatCombo) {
        int index = featureExprs.indexOf(featureExpr);
        unsatVertexCombo.add(index);
      }

      unsatVertexCombos.add(unsatVertexCombo);
    }

    return unsatVertexCombos;
  }

  private static Set<Set<FeatureExpr>> getUnsatCombos(List<Set<FeatureExpr>> featureExprsCombos) {
    Set<Set<FeatureExpr>> unsatCombos = new HashSet<>();

    for (Set<FeatureExpr> featureExprsInCombo : featureExprsCombos) {
      if (isSatCombo(featureExprsInCombo)) {
        continue;
      }

      boolean isAlreadyCovered = false;

      for (Set<FeatureExpr> unsatCombo : unsatCombos) {
        if (featureExprsInCombo.containsAll(unsatCombo)) {
          isAlreadyCovered = true;

          break;
        }
      }

      if (!isAlreadyCovered) {
        unsatCombos.add(featureExprsInCombo);
      }
    }

    return unsatCombos;
  }

  private static boolean isSatCombo(Set<FeatureExpr> featureExprs) {
    FeatureExpr formula = FeatureExprFactory.True();

    for (FeatureExpr featureExpr : featureExprs) {
      formula = formula.and(featureExpr);
    }

    return formula.isSatisfiable();
  }

  private static List<Set<FeatureExpr>> getFeatureExprsCombos(List<FeatureExpr> featureExprs) {
    List<Set<FeatureExpr>> combos = new ArrayList<>();
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
      Set<Pair<Integer, Integer>> mutexVertices,
      List<String> constraints) {

    int colors = 0;

    while (true) {
      System.out.println("Trying num of colors: " + (colors + 1));
      Set<SingleFeatureExpr> interestingFeatures = assignColorsToVertices(featureExprs, colors);

      FeatureExpr colorsFormula = buildColoredFormula(featureExprs, colors);
      FeatureExpr form = buildForm(featureExprs, colors, constraints);
//      FeatureExpr mutexFormula = buildMutexFormula(mutexVertices, colors);
      //      FeatureExpr equivFormula = buildEquivFormula(featureExprs, colors);

      FeatureExpr formula = colorsFormula.and(form);
      //      formula = formula.and(equivFormula);

      System.out.println("Formula size: " + formula.size());
      System.out.println();
      //      DimacsWriter dw = DimacsWriter.instance;
      //
      //      try {
      //        dw.printToDimacsDirect(formula, "work");
      //        throw new RuntimeException("work");
      //      } catch (AssertionError ae) {
      //        colors++;
      //      }
      //    }

      ////      formula
      //
      ////      ((SATFeatureExpr) formula).
      //
      if (!formula.isSatisfiable()) {
        colors++;

        continue;
      }

      return getColoringAssign(formula, interestingFeatures);
    }
  }

  private static FeatureExpr buildForm(
      List<FeatureExpr> featureExprs, int colors, List<String> constraints) {
    FeatureExpr form = FeatureExprFactory.True();

    for (int color = 0; color <= colors; color++) {

      for (int vertex = 0; vertex < featureExprs.size(); vertex++) {
        FeatureExpr coloredVertex = SAT_PARSER.parse(createVertex(vertex, color));
        FeatureExpr featureExpr = featureExprs.get(vertex);
        Iterator<String> tmp = featureExpr.collectDistinctFeatures().iterator();
        Set<String> features = new HashSet<>();

        while (tmp.hasNext()) {
          features.add(tmp.next());
        }

        String constraint = constraints.get(vertex);

        for (String f : features) {
          constraint = constraint.replaceAll(f, f + "_" + color);
        }

        FeatureExpr x = BDDFeatureExprParser.parseFeatureExprAsBDD(constraint);
        FeatureExpr y = ((BDDFeatureExpr) x).toSATFeatureExpr();

        FeatureExpr z = coloredVertex.implies(y);
        form = form.and(z);
      }
    }

    FeatureExprFactory.setDefault(FeatureExprFactory.sat());

    return form;
  }

  private static FeatureExpr buildEquivFormula(List<FeatureExpr> featureExprs, int colors) {
    FeatureExpr equivFormula = FeatureExprFactory.True();

    for (int vertex = 0; vertex < featureExprs.size(); vertex++) {
      for (int color = 0; color <= colors; color++) {
        FeatureExpr coloredVertex = SAT_PARSER.parse(createVertex(vertex, color));
        FeatureExpr equivFeatureExpr = coloredVertex.implies(featureExprs.get(vertex));

        equivFormula = equivFormula.and(equivFeatureExpr);
      }
    }

    return equivFormula;
  }

  private static FeatureExpr buildMutexFormula(
      Set<Pair<Integer, Integer>> mutexVertices, int colors) {
    FeatureExpr unsatCombosConstraints = FeatureExprFactory.True();

    for (int color = 0; color <= colors; color++) {
      FeatureExpr unsatCombosConstraint = FeatureExprFactory.True();

      for (Pair<Integer, Integer> mutexVertex : mutexVertices) {
        FeatureExpr coloredVertex1 = SAT_PARSER.parse(createVertex(mutexVertex.getLeft(), color));
        FeatureExpr coloredVertex2 = SAT_PARSER.parse(createVertex(mutexVertex.getRight(), color));
        FeatureExpr mutexFeatureExpr = coloredVertex1.and(coloredVertex2).not();

        unsatCombosConstraint = unsatCombosConstraint.and(mutexFeatureExpr);
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

  private static Set<SingleFeatureExpr> assignColorsToVertices(
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

  private static String createVertex(int vertex, int color) {
    return "v" + vertex + "_" + color;
  }

  private static FeatureExpr buildColoredFormula(List<FeatureExpr> featureExprs, int colors) {
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

  private static Set<SingleFeatureExpr> parseSingleFeatureExprs(Set<String> options) {
    Set<SingleFeatureExpr> singleFeatureExprs = new HashSet<>();

    for (String option : options) {
      SingleFeatureExpr featureExpr = (SingleFeatureExpr) SAT_PARSER.parse(option);
      singleFeatureExprs.add(featureExpr);
    }

    return singleFeatureExprs;
  }
}
