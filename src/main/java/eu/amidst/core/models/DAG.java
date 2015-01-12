package eu.amidst.core.models;

import eu.amidst.core.utils.Utils;
import eu.amidst.core.variables.StaticVariables;
import eu.amidst.core.variables.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Hanen on 13/11/14.
 */
public class DAG {

    private StaticVariables variables;
    private List<ParentSet> parents;

    public DAG(StaticVariables variables) {
        this.variables = variables;
        this.parents = new ArrayList(variables.getNumberOfVars());

        for (Variable var : variables) {
            parents.add(var.getVarID(), new ParentSetImpl(var));
        }
        this.parents = Collections.unmodifiableList(parents);
        this.variables.block();
    }

    public StaticVariables getStaticVariables() {
        return this.variables;
    }

    public ParentSet getParentSet(Variable var) {
        return parents.get(var.getVarID());
    }

    public boolean containCycles() {

        boolean[] bDone = new boolean[this.variables.getNumberOfVars()];

        for (Variable var : this.variables) {
            bDone[var.getVarID()] = false;
        }

        for (Variable var : this.variables) {

            // find a node for which all parents are 'done'
            boolean bFound = false;

            for (Variable variable2 : this.variables) {
                if (!bDone[variable2.getVarID()]) {
                    boolean bHasNoParents = true;

                    for (Variable parent : this.getParentSet(variable2)) {
                        if (!bDone[parent.getVarID()]) {
                            bHasNoParents = false;
                        }
                    }

                    if (bHasNoParents) {
                        bDone[variable2.getVarID()] = true;
                        bFound = true;
                        break;
                    }
                }
            }

            if (!bFound) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DAG dag = (DAG) o;

        if (this.variables.getNumberOfVars() != dag.variables.getNumberOfVars()) {
            return false;
        } else {
            boolean eqs = true;
            for (Variable var : this.getStaticVariables()) {
                if (!this.getParentSet(var).equals(dag.getParentSet(dag.getStaticVariables().getVariableByName(var.getName())))) {
                    eqs = false;
                    break;
                }
            }
            return eqs;
        }
    }


    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("DAG\n");
        for (Variable var : this.getStaticVariables()) {
            str.append(var.getName() + " parent sets: " + this.getParentSet(var).toString() + "\n");
        }
        return str.toString();
    }

    public List<ParentSet> getParentSets() {
        return this.parents;
    }

    private static final class ParentSetImpl implements ParentSet {

        private Variable mainVar;
        private List<Variable> vars;

        private ParentSetImpl(Variable mainVar1) {
            mainVar = mainVar1;
            this.vars = new ArrayList<Variable>();
        }

        @Override
        public Variable getMainVar() {
            return mainVar;
        }

        public void addParent(Variable var) {
            if (!Utils.isLinkCLG(mainVar, var)) {
                throw new IllegalArgumentException("Adding a Gaussian variable as parent of a Multinomial variable");
            }

            if (this.contains(var)) {
                throw new IllegalArgumentException("Trying to add a duplicated parent");
            }

            vars.add(var);
        }

        public void removeParent(Variable var) {
            vars.remove(var);
        }

        public List<Variable> getParents() {
            return vars;
        }

        public int getNumberOfParents() {
            return vars.size();
        }

        @Override
        public String toString() {

            int numParents = getNumberOfParents();

            StringBuilder str = new StringBuilder();
            str.append("{ ");

            for (int i = 0; i < numParents; i++) {
                Variable parent = getParents().get(i);
                str.append(parent.getName());
                if (i < numParents - 1) {
                    str.append(", ");
                }
            }
            str.append(" }");
            return str.toString();
        }

        /**
         * Is an ArrayList pointer to an ArrayList unmodifiable object still unmodifiable? I guess so right?
         */
        public void blockParents() {
            vars = Collections.unmodifiableList(vars);
        }

        public boolean contains(Variable var) {
            return this.vars.contains(var);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ParentSet parentset = (ParentSet) o;

            if (this.getNumberOfParents() != parentset.getNumberOfParents()) {
                return false;
            } else {
                int i = 0;
                boolean eqs = true;
                while (i < this.getNumberOfParents() && eqs) {
                    if (this.getParents().get(i).equals(parentset.getParents().get(i))) {
                        i++;
                    } else {
                        eqs = false;
                    }
                }
                return eqs;
            }
        }
    }


    public static void main(String[] args) throws Exception {
        BayesianNetwork network = BayesianNetworkLoader.loadFromHugin("./networks/asia.net");

        System.out.println(network.getDAG().getParentSets().parallelStream().mapToInt( p -> p.getNumberOfParents()).sum());

        System.out.println(network.getDAG().getParentSets().parallelStream().mapToInt( p -> p.getNumberOfParents()).sum());

    }
}