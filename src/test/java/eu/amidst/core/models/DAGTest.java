package eu.amidst.core.models;


import eu.amidst.core.datastream.filereaders.arffFileReader.ARFFDataReader;
import eu.amidst.core.variables.*;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Created by Hanen on 14/11/14.
 */
public class DAGTest {

/* Very simple example to test the DAG class*/



    @Test
    public void testingDAG() {

        ARFFDataReader reader = new ARFFDataReader();
        reader.loadFromFile("data/dataWeka/contact-lenses.arff");
        StaticVariables variables = new StaticVariables(reader.getAttributes());
        DAG dag = new DAG(variables);
        DAG dag2 = new DAG(variables);

        variables = dag.getStaticVariables();
        Variable A = variables.getVariableById(0);
        Variable B = variables.getVariableById(1);
        Variable C = variables.getVariableById(2);
        Variable D = variables.getVariableById(3);
        Variable E = variables.getVariableById(4);

        /* test cyclic dag */

        dag.getParentSet(A).addParent(C);
        dag.getParentSet(B).addParent(A);
        dag.getParentSet(C).addParent(B);
        dag.getParentSet(D).addParent(B);
        dag.getParentSet(E).addParent(B);

        assertTrue(dag.containCycles());

        /*remove the cycle and test again */

        dag.getParentSet(A).removeParent(C);
        dag.getParentSet(C).addParent(A);

        assertFalse(dag.containCycles());


        /*test the parent set*/

        assertEquals(2, dag.getParentSet(C).getNumberOfParents());
        assertEquals(0, dag.getParentSet(A).getNumberOfParents());

        /* test if dag and dag2 (no arcs) are equals*/

        assertFalse(dag.equals(dag2));

        /* define dag2 as a copy of dag and test again */

        dag2.getParentSet(B).addParent(A);
        dag2.getParentSet(C).addParent(B);
        dag2.getParentSet(D).addParent(B);
        dag2.getParentSet(E).addParent(B);
        dag2.getParentSet(C).addParent(A);

        assertTrue(dag.equals(dag2));

    }

}
