/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package eu.amidst.flinklink.examples;

import eu.amidst.core.datastream.Attributes;
import eu.amidst.core.datastream.DataInstance;
import eu.amidst.core.datastream.DataStream;
import eu.amidst.core.inference.messagepassing.VMP;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.models.DAG;
import eu.amidst.core.utils.BayesianNetworkGenerator;
import eu.amidst.core.variables.Variable;
import eu.amidst.core.variables.Variables;
import eu.amidst.flinklink.core.data.DataFlink;
import eu.amidst.flinklink.core.io.DataFlinkLoader;
import eu.amidst.flinklink.core.io.DataFlinkWriter;
import eu.amidst.flinklink.core.learning.parametric.ParallelVB;
import eu.amidst.flinklink.core.utils.BayesianNetworkSampler;
import org.apache.flink.api.java.ExecutionEnvironment;

/**
 * Created by Hanen on 08/10/15.
 */
public class ParallelVMPExtended {

    /**
     * Creates a {@link DAG} object with a naive Bayes structure from a given {@link DataStream}.
     * The main variable is defined as a latent binary variable which is set as a parent of all the observed variables.
     * @return a {@link DAG} object.
     */
    public static DAG getHiddenNaiveBayesStructure(Attributes attributes) {

        // Create a Variables object from the attributes of the input data stream.
        Variables modelHeader = new Variables(attributes);

        // Define the global latent binary variable.
        Variable globalHiddenVar = modelHeader.newMultionomialVariable("GlobalHidden", 2);

        // Define the global Gaussian latent binary variable.
        Variable globalHiddenGaussian = modelHeader.newGaussianVariable("globalHiddenGaussian");

        // Define the class variable.
        Variable classVar = modelHeader.getVariableById(0);

        // Create a DAG object with the defined model header.
        DAG dag = new DAG(modelHeader);

        // Define the structure of the DAG, i.e., set the links between the variables.
        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .filter(w -> w.getMainVar() != globalHiddenGaussian)
                .filter(w -> w.getMainVar().isMultinomial())
                .forEach(w -> w.addParent(globalHiddenVar));

        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .filter(w -> w.getMainVar() != globalHiddenVar)
                .filter(w -> w.getMainVar() != globalHiddenGaussian)
                .filter(w -> w.getMainVar().isNormal())
                .forEach(w -> w.addParent(globalHiddenGaussian));

        dag.getParentSets()
                .stream()
                .filter(w -> w.getMainVar() != classVar)
                .forEach(w -> w.addParent(classVar));

        // Return the DAG.
        return dag;
    }

    public static void main2(String[] args) throws Exception {



    }

    /**
     *
     * ./bin/flink run -m yarn-cluster -yn 2 -ys 4 -yjm 1024 -ytm 5000 -c eu.amidst.flinklink.examples.ParallelVMPExtended ../flinklink.jar 50 50 10000 100 10 100
     *
     * yn  = 1, 2, 4, 8, 16
     *
     * samples = 100000
     *
     * windowSize = 1000
     *
     * globalIter = 1000
     *
     * localIter = 10
     *
     * Other test with windowSize = 100
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        int nCVars = Integer.parseInt(args[0]);
        int nMVars = Integer.parseInt(args[1]);
        int nSamples = Integer.parseInt(args[2]);
        int windowSize = Integer.parseInt(args[3]);
        int globalIter = Integer.parseInt(args[4]);
        int localIter = Integer.parseInt(args[5]);

        String fileName = "hdfs:///tmp"+nCVars+"_"+nMVars+"_"+nSamples+"_"+windowSize+"_"+globalIter+"_"+localIter+".arff";

        // Randomly generate the data stream using {@link BayesianNetworkGenerator} and {@link BayesianNetworkSampler}.
        BayesianNetworkGenerator.setNumberOfGaussianVars(nCVars);
        BayesianNetworkGenerator.setNumberOfMultinomialVars(nMVars, 2);
        BayesianNetwork originalBnet  = BayesianNetworkGenerator.generateBayesianNetwork();

        //Sampling from Asia BN
        BayesianNetworkSampler sampler = new BayesianNetworkSampler(originalBnet);
        sampler.setSeed(0);

        //Load the sampled data
        DataFlink<DataInstance> data = sampler.sampleToDataFlink(nSamples);

        DataFlinkWriter.writeDataToARFFFolder(data,fileName);

        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataFlink<DataInstance> dataFlink = DataFlinkLoader.loadDataFromFolder(env,fileName, false);

        DAG hiddenNB = getHiddenNaiveBayesStructure(dataFlink.getAttributes());


        //Structure learning is excluded from the test, i.e., we use directly the initial Asia network structure
        // and just learn then test the parameter learning

        long start = System.nanoTime();

        //Parameter Learning
        ParallelVB parallelVB = new ParallelVB();
        parallelVB.setMaximumGlobalIterations(globalIter);
        parallelVB.setSeed(5);

        //Set the window size
        parallelVB.setBatchSize(windowSize);
        VMP vmp = parallelVB.getSVB().getPlateuStructure().getVMP();
        vmp.setTestELBO(true);
        vmp.setMaxIter(localIter);
        vmp.setThreshold(0.001);

        parallelVB.setDAG(hiddenNB);
        parallelVB.setDataFlink(dataFlink);
        parallelVB.runLearning();
        BayesianNetwork LearnedBnet = parallelVB.getLearntBayesianNetwork();
        System.out.println(LearnedBnet.toString());

        long duration = (System.nanoTime() - start) / 1;
        double seconds = duration / 1000000000.0;
        System.out.println("Running time: \n" + seconds + " secs");
        System.out.println("Global ELBO:" + parallelVB.getLogMarginalProbability());

    }

    }