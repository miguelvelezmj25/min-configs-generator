package edu.cmu.cs.mvelezce;

import de.fosd.typechef.featureexpr.*;
import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr;
import de.fosd.typechef.featureexpr.sat.SATFeatureModel;
import edu.cmu.cs.mvelezce.parser.bdd.BDDFeatureExprParser;
import edu.cmu.cs.mvelezce.parser.sat.SATFeatureExprParser;
import scala.Option;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.JavaConverters;

import java.util.*;

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
   * @param stringConstraints the partial configurations.
   * @return
   */
  public static Set<Set<String>> getSatConfigs(
      Set<String> options, List<String> stringConstraints) {
    List<FeatureExpr> bddFeatureExprs = parseAsBDDFeatureExprs(stringConstraints);

    Set<Integer> indexesOfTautologies = getIndexesOfTautologies(bddFeatureExprs);
    stringConstraints = removeStringConstraints(stringConstraints, indexesOfTautologies);
    bddFeatureExprs = removeBDDFeatureExprs(bddFeatureExprs, indexesOfTautologies);

    Set<Integer> indexesOfRedundantConstraints = getIndexesOfEquivalentConstraints(bddFeatureExprs);
    stringConstraints = removeStringConstraints(stringConstraints, indexesOfRedundantConstraints);
    bddFeatureExprs = removeBDDFeatureExprs(bddFeatureExprs, indexesOfRedundantConstraints);

    List<FeatureExpr> featureExprs =
        SATFeatureExprParser.BDDFeatureExprstoSatFeatureExprs(bddFeatureExprs);
    Collection<SingleFeatureExpr> coloring = getColoring(featureExprs, stringConstraints);

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

  public static FeatureExpr parseAsSATFeatureExpr(String constraint) {
    BDDFeatureExpr bbdFeatureExpr =
        (BDDFeatureExpr) BDDFeatureExprParser.parseFeatureExprAsBDD(constraint);

    return bbdFeatureExpr.toSATFeatureExpr();
  }

  public static BDDFeatureExpr parseAsBDDFeatureExpr(String constraint) {
    BDDFeatureExpr bbdFeatureExpr =
        (BDDFeatureExpr) BDDFeatureExprParser.parseFeatureExprAsBDD(constraint);

    return bbdFeatureExpr;
  }

  public static List<FeatureExpr> getFeatureExprs(List<String> stringConstraints) {
    System.err.println(
        "Seems weird to be using a \'min config generator\' tool to parse feature expr");
    List<FeatureExpr> bddFeatureExprs = parseAsBDDFeatureExprs(stringConstraints);

    Set<Integer> indexesOfTautologies = getIndexesOfTautologies(bddFeatureExprs);
    bddFeatureExprs = removeBDDFeatureExprs(bddFeatureExprs, indexesOfTautologies);

    Set<Integer> indexesOfRedundantConstraints = getIndexesOfEquivalentConstraints(bddFeatureExprs);
    bddFeatureExprs = removeBDDFeatureExprs(bddFeatureExprs, indexesOfRedundantConstraints);

    return SATFeatureExprParser.BDDFeatureExprstoSatFeatureExprs(bddFeatureExprs);
  }

  private static List<String> removeStringConstraints(
      List<String> stringConstraints, Set<Integer> indexesToRemove) {
    List<String> newStringConstraints = new ArrayList<>();

    for (int i = 0; i < stringConstraints.size(); i++) {
      if (!indexesToRemove.contains(i)) {
        newStringConstraints.add(stringConstraints.get(i));
      }
    }

    return newStringConstraints;
  }

  private static Set<Integer> getIndexesOfEquivalentConstraints(List<FeatureExpr> bddFeatureExprs) {
    Set<Integer> indexesOfRedundantConstraints = new HashSet<>();

    for (int i = 0; i < (bddFeatureExprs.size() - 1); i++) {
      FeatureExpr featureExpr1 = bddFeatureExprs.get(i);

      for (int j = (i + 1); j < bddFeatureExprs.size(); j++) {
        FeatureExpr featureExpr2 = bddFeatureExprs.get(j);

        if (featureExpr1.equivalentTo(featureExpr2)) {
          indexesOfRedundantConstraints.add(i);

          break;
        }
      }
    }

    return indexesOfRedundantConstraints;
  }

  private static List<FeatureExpr> removeBDDFeatureExprs(
      List<FeatureExpr> bddFeatureExprs, Set<Integer> indexesToRemove) {
    List<FeatureExpr> newBDDFeatureExprs = new ArrayList<>();

    for (int i = 0; i < bddFeatureExprs.size(); i++) {
      if (!indexesToRemove.contains(i)) {
        newBDDFeatureExprs.add(bddFeatureExprs.get(i));
      }
    }

    return newBDDFeatureExprs;
  }

  private static Set<Integer> getIndexesOfTautologies(List<FeatureExpr> bddFeatureExprs) {
    Set<Integer> indexesOfTautologies = new HashSet<>();

    for (int i = 0; i < bddFeatureExprs.size(); i++) {
      FeatureExpr bddFeatureExpr = bddFeatureExprs.get(i);

      if (bddFeatureExpr.isTautology()) {
        indexesOfTautologies.add(i);
      }
    }

    return indexesOfTautologies;
  }

  private static List<FeatureExpr> parseAsBDDFeatureExprs(List<String> stringConstraints) {
    List<FeatureExpr> bddFeatureExprs = new ArrayList<>();

    for (String stringConstraint : stringConstraints) {
      FeatureExpr bddFeatureExpr = BDDFeatureExprParser.parseFeatureExprAsBDD(stringConstraint);
      bddFeatureExprs.add(bddFeatureExpr);
    }

    return bddFeatureExprs;
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
      List<FeatureExpr> featureExprs, List<String> stringConstraints) {

    int colors = 0;

    while (true) {
      System.out.println("Trying num of colors: " + (colors + 1));
      FeatureExpr colorsFormula = buildColoredFormula(featureExprs, colors);
      FeatureExpr constraintFormula =
          buildConstraintFormula(featureExprs, colors, stringConstraints);
      FeatureExpr formula = colorsFormula.and(constraintFormula);
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

      Set<SingleFeatureExpr> interestingFeatures = assignColorsToVertices(featureExprs, colors);

      return getColoringAssign(formula, interestingFeatures);
    }
  }

  private static FeatureExpr buildConstraintFormula(
      List<FeatureExpr> featureExprs, int colors, List<String> stringConstraints) {
    FeatureExpr constraintFormula = FeatureExprFactory.True();

    for (int color = 0; color <= colors; color++) {
      for (int vertex = 0; vertex < featureExprs.size(); vertex++) {
        FeatureExpr featureExpr = featureExprs.get(vertex);
        Set<String> features = getDistinctFeatures(featureExpr);
        String constraint = stringConstraints.get(vertex);

        for (String feature : features) {
          constraint = constraint.replaceAll(feature, feature + "_" + color);
        }

        FeatureExpr bddFeatureExpr = BDDFeatureExprParser.parseFeatureExprAsBDD(constraint);
        FeatureExpr satFeatureExpr = ((BDDFeatureExpr) bddFeatureExpr).toSATFeatureExpr();

        FeatureExpr coloredVertex = SAT_PARSER.parse(createVertex(vertex, color));
        FeatureExpr implication = coloredVertex.implies(satFeatureExpr);
        constraintFormula = constraintFormula.and(implication);
      }
    }

    // TODO MIGUEL hacky way of setting back to using a SAT solver since I used BDD
    FeatureExprFactory.setDefault(FeatureExprFactory.sat());

    return constraintFormula;
  }

  private static Set<String> getDistinctFeatures(FeatureExpr featureExpr) {
    Iterator<String> distinctFeaturesIter = featureExpr.collectDistinctFeatures().iterator();
    Set<String> features = new HashSet<>();

    while (distinctFeaturesIter.hasNext()) {
      features.add(distinctFeaturesIter.next());
    }

    return features;
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
