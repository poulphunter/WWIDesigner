package com.wwidesigner.optimization;

import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.direct.BOBYQAOptimizer;
import org.apache.commons.math3.optimization.direct.BaseAbstractMultivariateSimpleBoundsOptimizer;
import org.apache.commons.math3.optimization.direct.CMAESOptimizer;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

public abstract class InstrumentOptimizer implements
		InstrumentOptimizerInterface
{
	protected TuningInterface tuning;
	protected double[] lowerBnd;
	protected double[] upperBnd;
	protected OptimizationFunctionInterface optimizationFunction;
	protected Instrument instrument;
	protected InstrumentCalculator instrumentCalculator;
	@SuppressWarnings("rawtypes")
	protected BaseAbstractMultivariateSimpleBoundsOptimizer baseOptimizer;
	protected OptimizerType baseOptimizerType;
	protected int numberOfInterpolationPoints;

	public abstract void setOptimizationFunction();

	public InstrumentOptimizer(int numberOfInterpolationPoints,
			Instrument inst, InstrumentCalculator calculator,
			TuningInterface tuning)
	{
		// Default to a BOBYQAOptimizer
		// The number of interpolation point
		// should be set according
		// to the number of variables in the optimization problem,
		// which depends on the OptimizableInstrument
		setBaseOptimizer(OptimizerType.BOBYQAOptimizer,
				numberOfInterpolationPoints);

		this.instrument = inst;
		this.instrumentCalculator = calculator;
		this.tuning = tuning;
	}

	/**
	 * @param lowerBound
	 *            the lowerBound to set
	 */
	public void setLowerBnd(double[] lowerBound)
	{
		this.lowerBnd = lowerBound;
	}

	/**
	 * @param upperBound
	 *            the upperBound to set
	 */
	public void setUpperBnd(double[] upperBound)
	{
		this.upperBnd = upperBound;
	}

	public void setBaseOptimizer(OptimizerType baseOptimizerType,
			int numberOfInterpolationPoints)
	{
		this.numberOfInterpolationPoints = numberOfInterpolationPoints;

		switch (baseOptimizerType)
		{
			case BOBYQAOptimizer:
				baseOptimizer = new BOBYQAOptimizer(numberOfInterpolationPoints);
				baseOptimizerType = OptimizerType.BOBYQAOptimizer;
				break;
			case CMAESOptimizer:
				baseOptimizer = new CMAESOptimizer(numberOfInterpolationPoints);
				baseOptimizerType = OptimizerType.CMAESOptimizer;
				break;
		}
	}

	/**
	 * @return the instrument
	 */
	public Instrument getInstrument()
	{
		return instrument;
	}

	/**
	 * @return the instrument calculator
	 */
	public InstrumentCalculator getInstrumentCalculator()
	{
		return instrumentCalculator;
	}

	@SuppressWarnings("unchecked")
	public void optimizeInstrument()
	{
		double[] startPoint = getStateVector();
		setOptimizationFunction();
		double startError = optimizationFunction.calculateErrorNorm();
		baseOptimizer.optimize(25000, optimizationFunction, GoalType.MINIMIZE,
				startPoint, lowerBnd, upperBnd);
		double endError = optimizationFunction.calculateErrorNorm();
		int iterations = optimizationFunction.getIterationsDone();
		System.out.println("Optimization residual: " + endError / startError
				+ " in " + iterations + " iterations");
	}

	public enum OptimizerType
	{
		BOBYQAOptimizer, CMAESOptimizer

	}

}
