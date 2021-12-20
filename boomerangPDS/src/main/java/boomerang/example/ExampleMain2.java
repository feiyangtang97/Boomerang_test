/**
 * ***************************************************************************** Copyright (c) 2018
 * Fraunhofer IEM, Paderborn, Germany. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * <p>SPDX-License-Identifier: EPL-2.0
 *
 * <p>Contributors: Johannes Spaeth - initial API and implementation
 * *****************************************************************************
 */
package boomerang.example;

import boomerang.Boomerang;
import boomerang.DefaultBoomerangOptions;
import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.results.ForwardBoomerangResults;
import boomerang.scene.AllocVal;
import boomerang.scene.AnalysisScope;
import boomerang.scene.ControlFlowGraph.Edge;
import boomerang.scene.SootDataFlowScope;
import boomerang.scene.Statement;
import boomerang.scene.Val;
import boomerang.scene.jimple.BoomerangPretransformer;
import boomerang.scene.jimple.SootCallGraph;
import com.google.common.collect.Table;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Transformer;
import soot.options.Options;
import wpds.impl.Weight.NoWeight;

public class ExampleMain2 {
	
public static String jarDirectory = System.getProperty("user.dir") + File.separator + "input";
public String testCp;
	
	public static Set<String> getAllClassesFromDirectory(String dir) throws IOException {
		Set<String> classes = new HashSet<String>();
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].getName().endsWith(".jar") || listOfFiles[i].getName().endsWith(".apk"))
					classes.addAll(getAllClassesFromJar(listOfFiles[i].getAbsolutePath()));
			}
		}
		return classes;
	}
	
	public static Set<String> getAllClassesFromJar(String jarFile) throws IOException {
		Set<String> classes = new HashSet<String>();
		ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile));
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
				String className = entry.getName().replace('/', '.');
				className = className.substring(0, className.length() - ".class".length());
				if (className.contains("$"))
					className = className.substring(0, Math.max(className.indexOf("$") - 1, className.length()));
				classes.add(className);
			}
		}
		zip.close();
		return classes;
	}
	
	
	/*
	public static void main(String... args) {
	    String sootClassPath = getSootClassPath();
	    String mainClass = "boomerang.example.read";
	    setupSoot(sootClassPath, mainClass);
	    analyze();
	  }*/
	
	
  public static void main(String... args) throws Exception {
	  String testCp = buildCP(jarDirectory);
	  Set<String> testClasses = getAllClassesFromDirectory(jarDirectory);
	  String sootClassPath = getSootClassPath(testCp);
	  System.out.println(sootClassPath);
	  setupSoot(sootClassPath);
	    for (String className : testClasses) {
	    	System.out.println(className);
	    	loadTest(className);
	    	analyze();
	    }
  }
  
  public static String buildCP(String dir) {
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		StringBuilder sb = new StringBuilder();
		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].getName().endsWith(".jar") || listOfFiles[i].getName().endsWith(".apk")) {
					if (sb.length() > 0) {
						sb.append(System.getProperty("path.separator"));
					}
					sb.append(listOfFiles[i].getAbsolutePath().toString());
				}
			}
		}
		return sb.toString();
	}

  private static String getSootClassPath(String testCp) {
    // Assume target folder to be directly in user dir; this should work in eclipse
        //System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes";
    File classPathDir = new File(testCp);
    if (!classPathDir.exists()) {
        // We haven't found our bytecode anyway, notify now instead of starting analysis anyway
        throw new RuntimeException("Classpath could not be found.");
    }
    return testCp;
  }

  private static void setupSoot(String sootClassPath) {
    G.v();
	G.reset();
	
    Options.v().set_prepend_classpath(true);
    Options.v().set_whole_program(true);
    Options.v().set_include_all(true);
    Options.v().setPhaseOption("cg.spark", "on");
    Options.v().set_output_format(Options.output_format_none);
    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().include_all();
/*
    List<String> includeList = new LinkedList<String>();
    includeList.add("java.lang.*");
    includeList.add("java.util.*");
    includeList.add("java.io.*");
    includeList.add("sun.misc.*");
    includeList.add("java.net.*");
    includeList.add("javax.servlet.*");
    includeList.add("javax.crypto.*");

    Options.v().set_include(includeList);*/
    Options.v().setPhaseOption("jb", "use-original-names:true");

    Options.v().set_soot_classpath(sootClassPath);
    Options.v().set_prepend_classpath(true);
    // Options.v().set_main_class(this.getTargetClass());
    Scene.v().loadNecessaryClasses();
  }
  
  private static void loadTest(String mainClass) {
	  SootClass c = Scene.v().forceResolve(mainClass, SootClass.BODIES);

	    if (c != null) {
	      c.setApplicationClass();
	    }
	    /*
	 // Force resolve inner classes, as the setup does currently not load them automatically.
	    c = Scene.v().forceResolve(mainClass + "$NestedClassWithField", SootClass.BODIES);
	    c.setApplicationClass();

	    c = Scene.v().forceResolve(mainClass + "$ClassWithField", SootClass.BODIES);
	    c.setApplicationClass();
	    */
	    for (SootMethod m : c.getMethods()) {
	        System.out.println(m);
	      }
  }

  private static void analyze() {
    Transform transform = new Transform("wjtp.ifds", createAnalysisTransformer());
    PackManager.v().getPack("wjtp").add(transform);
    PackManager.v().getPack("cg").apply();
    BoomerangPretransformer.v().apply();
    PackManager.v().getPack("wjtp").apply();
  }

  private static Transformer createAnalysisTransformer() {
    return new SceneTransformer() {
      protected void internalTransform(
          String phaseName, @SuppressWarnings("rawtypes") Map options) {
        SootCallGraph sootCallGraph = new SootCallGraph();
        AnalysisScope scope =
            new AnalysisScope(sootCallGraph) {
              @Override
              protected Collection<? extends Query> generate(Edge cfgEdge) {
                Statement statement = cfgEdge.getStart();
                if (statement.getMethod().toString().contains("r")
                		&&!statement.getMethod().getName().contains("init")
                    && statement.getMethod().isConstructor()
                    && statement.isAssign()) {
                  if (statement.getRightOp().isIntConstant()) {
                    return Collections.singleton(
                        new ForwardQuery(
                            cfgEdge,
                            new AllocVal(
                                statement.getLeftOp(), statement, statement.getRightOp())));
                  }
                }
                return Collections.emptySet();
              }
            };

        Collection<Query> seeds = scope.computeSeeds();
        for (Query query : seeds) {
          // 1. Create a Boomerang solver.
          Boomerang solver =
              new Boomerang(
                  sootCallGraph, SootDataFlowScope.make(Scene.v()), new DefaultBoomerangOptions());
          System.out.println("Solving query: " + query);
          // 2. Submit a query to the solver.
          ForwardBoomerangResults<NoWeight> forwardBoomerangResults =
              solver.solve((ForwardQuery) query);

          // 3. Process forward results
          Table<Edge, Val, NoWeight> results = forwardBoomerangResults.asStatementValWeightTable();
          for (Edge s : results.rowKeySet()) {
        	  System.out.println(s);
            // 4. Filter results based on your use statement, in our case the call of
            //if (s.getTarget().toString().contains("print")) {
        	  if(true) {
              // 5. Check that a propagated value is used at the particular statement.
              for (Val reachingVal : results.row(s).keySet()) {
                if (s.getTarget().uses(reachingVal)) {
                  System.out.println(query + " reaches " + s);
                }
              }
            }
          }
        }
      }
    };
  }
}
