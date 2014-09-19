package eu.amidst.core.DynamicBayesianNetwork.impl;

import eu.amidst.core.DynamicBayesianNetwork.DynamicBayesianNetwork;
import eu.amidst.core.StaticBayesianNetwork.ParentSet;
import eu.amidst.core.headers.DynamicModelHeader;
import eu.amidst.core.headers.DynamicDataHeader;

import eu.amidst.core.Estimators.Estimator;
import eu.amidst.core.headers.DynamicVariable;
import eu.amidst.core.headers.Variable;

/**
 * Created by afa on 03/07/14.
 */
public class DynamicBayesNetworkImpl implements DynamicBayesianNetwork {
    private ParentSet[][] parentSetTime0;
    private ParentSet[] parentSetTimeT;
    private Estimator[][] estimatorTime0;
    private Estimator[] estimatorTimeT;
    private DynamicModelHeader modelHeader;

    public DynamicBayesNetworkImpl(DynamicModelHeader modelHeader){

    }


    @Override
    public int getMarkovOrder() {
        return this.modelHeader.getMarkovOrder();
    }

    @Override
    public void initEstimators() {

    }

    @Override
    public int getNumberOfNodes() {
        return this.modelHeader.getNumberOfVars();
    }

    @Override
    public DynamicModelHeader getDynamicModelHeader() {
        return this.modelHeader;
    }

    @Override
    public DynamicVariable getVariableById(int varID) {
        return this.modelHeader.getVariableById(varID);
    }

    @Override
    public DynamicVariable getVariableByTimeId(int varTimeID) {
        return this.getVariableByTimeId(varTimeID);
    }

    @Override
    public ParentSet getParentSetTimeT(int varID) {
        return this.parentSetTimeT[varID];
    }

    @Override
    public Estimator getEstimatorTimeT(int varId) {
        return this.estimatorTimeT[varId];
    }

    @Override
    public ParentSet getParentSetTime0(int varID) {
        return this.parentSetTime0[0][varID];
    }

    @Override
    public Estimator getEstimatorTime0(int varId) {
        return this.estimatorTime0[0][varId];
    }

    @Override
    public ParentSet getParentSetTime0(int varID, int initTime) {
        return this.parentSetTime0[initTime][varID];
    }

    @Override
    public Estimator getEstimatorTime0(int varId, int initTime) {
        return this.estimatorTime0[initTime][varId];
    }

}