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

package eu.amidst.flinklink.core.utils;

import eu.amidst.core.datastream.DataInstance;
import eu.amidst.core.io.BayesianNetworkLoader;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.flinklink.core.data.DataFlink;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Created by andresmasegosa on 23/9/15.
 */
public class BayesianNetworkSamplerTest extends TestCase {

    public void test1() throws Exception {
        BayesianNetwork asianet = BayesianNetworkLoader.loadFromFile("networks/asia.bn");

        BayesianNetworkSampler sampler = new BayesianNetworkSampler(asianet);
        sampler.setBatchSize(2);

        DataFlink<DataInstance> data = sampler.sampleToDataFlink(10);

        System.out.println("--------");
        data.getDataSet().print();
        System.out.println("--------");
        data.getDataSet().print();
        System.out.println("--------");

    }
    public void testingAsia() throws Exception {

        BayesianNetwork asianet = BayesianNetworkLoader.loadFromFile("networks/asia.bn");

        BayesianNetworkSampler sampler = new BayesianNetworkSampler(asianet);

        DataFlink<DataInstance> data = sampler.sampleToDataFlink(10000);

        Assert.assertEquals(data.getAttributes().getNumberOfAttributes(),8);

        Assert.assertEquals(10000,data.getDataSet().count());

    }

}