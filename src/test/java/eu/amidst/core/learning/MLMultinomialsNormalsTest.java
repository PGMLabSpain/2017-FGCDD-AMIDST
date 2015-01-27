package eu.amidst.core.learning;

import COM.hugin.HAPI.ExceptionHugin;
import eu.amidst.core.database.DataBase;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.models.BayesianNetworkLoader;
import eu.amidst.core.utils.BayesianNetworkSampler;
import eu.amidst.core.variables.Variable;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by Hanen on 27/01/15.
 */
public class MLMultinomialsNormalsTest {

        @Test
        public void testingML2() throws ExceptionHugin, IOException, ClassNotFoundException {

            // load the true WasteIncinerator hugin Bayesian network containing 3 Multinomial and 6 Gaussian variables

            BayesianNetwork trueBN = BayesianNetworkLoader.loadFromFile("./networks/WasteIncinerator.ser");

            System.out.println("\nWasteIncinerator network \n ");
            System.out.println(trueBN.getDAG().toString());
            System.out.println(trueBN.toString());

            //Sampling from trueBN
            BayesianNetworkSampler sampler = new BayesianNetworkSampler(trueBN);
            sampler.setSeed(0);
            sampler.setParallelMode(true);

            //Load the sampled data
            DataBase data = sampler.sampleToDataBase(100000);

            //try{
            //    sampler.sampleToAnARFFFile("./data/WasteIncineratorSamples.arff", 10000);
            //} catch (IOException ex){
            //}
            //DataBase data = new StaticDataOnDiskFromFile(new ARFFDataReader(new String("data/WasteIncineratorSamples.arff")));

            //Structure learning is excluded from the test, i.e., we use directly the initial Asia network structure
            // and just learn then test the parameter learning

            //Parameter Learning
            MaximumLikelihood.setBatchSize(1000);
            MaximumLikelihood.setParallelMode(true);
            BayesianNetwork bnet = MaximumLikelihood.learnParametersStaticModel(trueBN.getDAG(), data);

            //Check if the probability distributions of each node
            for (Variable var : trueBN.getStaticVariables()) {
                System.out.println("\n------ Variable " + var.getName() + " ------");
                System.out.println("\nTrue distribution:\n"+ trueBN.getDistribution(var));
                System.out.println("\nLearned distribution:\n"+ bnet.getDistribution(var));
                assertTrue(bnet.getDistribution(var).equalDist(trueBN.getDistribution(var), 0.05));
            }

            //Or check directly if the true and learned networks are equals
            assertTrue(bnet.equalBNs(trueBN,0.05));
        }
}

