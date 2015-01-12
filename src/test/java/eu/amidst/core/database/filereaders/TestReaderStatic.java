/**
 ******************* ISSUE LIST **************************
 *
 * 1. Do we need a getVariableByName(String s) in StaticModelHeader?

 *
 * ********************************************************
 */

package eu.amidst.core.database.filereaders;


import eu.amidst.core.database.Attribute;
import eu.amidst.core.database.Attributes;
import eu.amidst.core.database.DataInstance;
import eu.amidst.core.database.DataOnDisk;
import eu.amidst.core.database.filereaders.arffFileReader.ARFFDataReader;
import eu.amidst.core.database.filereaders.arffWekaReader.WekaDataFileReader;

import eu.amidst.core.variables.StaticVariables;
import eu.amidst.core.variables.Variable;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by ana@cs.aau.dk on 17/11/14.
 */
public class TestReaderStatic {

    private static final double DELTA = 1e-15;
    private static ARFFDataReader reader;
    private static Attributes attributes;
    private static StaticVariables staticVariables;
    private static DataRow datarow = null;
    private static DataInstance nextInstance = null;
    private static int index;
    private static Variable var;
    private static DataOnDisk dataOnDiskReader;
    private static Iterator<DataInstance> dataOnDiskIterator;


    public static void loadFileAndInitializeStatic(){
        reader = new ARFFDataReader("data/dataWeka/labor.arff");
        dataOnDiskReader = new StaticDataOnDiskFromFile(reader);
        dataOnDiskIterator = dataOnDiskReader.iterator();
        attributes = dataOnDiskReader.getAttributes();
        staticVariables = new StaticVariables(attributes);
    }

    @Test
    public void loadArffWekaFileStatic() {
        reader = new ARFFDataReader("data/dataWeka/labor.arff");

        attributes = reader.getAttributes();

        assertEquals(17, attributes.getList().size());
    }

    @Test
    public void numericAttributeValue() {

        loadFileAndInitializeStatic();


        if(dataOnDiskIterator.hasNext()){
            nextInstance = dataOnDiskIterator.next();
        }

        /* Numeric attribute */
        assertEquals(5, (int) nextInstance.getValue(staticVariables.getVariableByName("wage-increase-first-year")));
    }

    @Test
    public void numericAttributeValue_DataOnDisk() {

        loadFileAndInitializeStatic();

        if (dataOnDiskIterator.hasNext()) {
            nextInstance = dataOnDiskIterator.next();
        }

        /* Numeric attribute */
        assertEquals(5, (int) nextInstance.getValue(staticVariables.getVariableByName("wage-increase-first-year")));
    }

    @Test
    public void discreteAttributeValue() {
        loadFileAndInitializeStatic();
        if (dataOnDiskIterator.hasNext()) {
            nextInstance = dataOnDiskIterator.next();
        }

        /* Discrete attribute */
        assertEquals(1, (int) nextInstance.getValue(staticVariables.getVariableByName("vacation")));
        /* Number of states */
        assertEquals(3, staticVariables.getVariableByName("pension").getNumberOfStates());
    }

    @Test
    public void missingValues() {

        loadFileAndInitializeStatic();

        /* Missing values (Get the 3rd instance) */
        if (dataOnDiskIterator.hasNext()) {
            dataOnDiskIterator.next();
            dataOnDiskIterator.next();
            nextInstance = dataOnDiskIterator.next();
        }

        var = staticVariables.getVariableByName("wage-increase-first-year");
        assertEquals(Double.NaN, nextInstance.getValue(var), DELTA);
    }

    @Test
    public void numberOfInstances_DataOnDisk() {

        loadFileAndInitializeStatic();

        /* Number of instances */
        int instanceCounter = 0;
        for (DataInstance dataInstance: dataOnDiskReader){
            instanceCounter++;
        }
        assertEquals(57, instanceCounter);
    }

    @Test
    public void numberOfInstances() {

        loadFileAndInitializeStatic();

        /* Number of instances */
        int instanceCounter = 0;
        for (DataRow row: reader){
            instanceCounter++;
        }
        assertEquals(57, instanceCounter);
    }



}