/**
 * Class to model the physical properties of air.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.util;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.util.Constants.TemperatureType;

/**
 * @author Edward Kort, Burton Patkau
 * 
 * References:
 * 
 *   P.T. Tsilingiris, "Thermophysical and transport properties of humid air
 *       at temperature range between 0 and 100 C",
 *       Energy Conversion and Management 49 (2008) p.1098-1110.
 *
 *   A. Picard, R.S. Davis, M. Glaser and K. Fujii,
 *       "Revised formula for the density of moist air (CIPM-2007)",
 *       Metrologia 45 (2008) p.149-155.
 *
 *   F.J. McQuillan, J.R. Culham, M.M. Yovanovich,
 *       "Properties of Dry Air at One Atmosphere", UW/MHTL 8406 G-01,
 *       Microelectronics Heat Transfer Lab, University of Waterloo, June 1984.
 */
public class PhysicalParameters
{
	// Input properties of the air.
	private double mTemperature;	// Temperature, in Celsius
	private double mPressure;		// Air pressure, in kPa
	private double m_xv;			// Molar fraction of water vapour, in mol/mol
	private double m_xCO2;			// Molar fraction of CO2, in mol/mol
	private double mHumidity;		// Relative humidity, as % of saturation humidity
	
	// Calculated properties of moist air.
	private double mRho;			// Air density, in kg/m^3
	private double mEta;			// Dynamic viscosity, in kg/(m.s)
	private double mSpecificHeat;	// Isobaric specific heat, in J/(kg.K)
	private double mGamma;			// Ratio of specific heats, cp/cv, dimensionless
	private double mKappa; 			// Thermal conductivity, in W/(m.K)
	private double mPrandtl;		// Prandtl number, dimensionless
	private double mSpeedOfSound;	// c, in m/s

    // Multiplier for calculating adjustment to complex wave number.
    // k = 2*pi*f/c * ( 1 + epsilon - 1j * epsilon )
    // where epsilon = mEpsilonConstant / (a*sqrt(f))
	private double mEpsilonConstant;
	// Alternatively,
	// k = 2*pi*f/v - j*alpha
	// where alpha = mAlphaConstant * sqrt(waveNumber) / a
	// and 2*pi*f/v ~= 2*pi*f/c + alpha
	private double mAlphaConstant;

	// Wave number, k, at 1 Hz: 2*pi/c, in radians per metre.
	private double mWaveNumber1;

	private static final double R    = 8.314472d;		// Universal gas constant J/mol K.
	private static final double Ma0  = 28.960745d;		// Standard molar mass of CO2-free dry air, kg/kmol.
	private static final double Mco2 = 44.0100d;			// Standard molar mass of CO2.
	private static final double Mo2  = 31.9988d;			// Standard molar mass of O2.
	private static final double Mv   = 18.01527d;		// Molar mass of water vapour, kg/kmol.

	public PhysicalParameters()
	{
		this(72.0d, TemperatureType.F);
	}

	public PhysicalParameters(double temperature, TemperatureType tempType)
	{
		this(temperature, tempType, 101.325d, 45.0d, 0.000390d);
	}

	/** Initialize physical parameters of air from specified properties.
	 * @param temperature - temperature, in degrees F or C
	 * @param tempType - temperature units, Fahrenheit or Celsius
	 * @param pressure - pressure, in kPa
	 * @param relHumidity - relative humidity, in percent of saturation humidity
	 * @param xCO2 - molar fraction of CO2 in air, in mol/mol
	 */
	public PhysicalParameters(double temperature, TemperatureType tempType,
			double pressure, double relHumidity, double xCO2)
	{
		double celsius;
		if (tempType == TemperatureType.F)
		{
			celsius = (temperature + 40.d) * 5.d / 9.d - 40.d;
		}
		else
		{
			celsius = temperature;
		}
		setProperties(celsius, pressure, relHumidity, xCO2);
	}
		
	/** Set the physical parameters of the air in the instrument,
	 * from specified properties.
	 * @param temperature - air temperature, in Celsius
	 * @param pressure - air pressure, in kPa
	 * @param relHumidity - relative humidity, in percent of saturation humidity
	 * @param xCO2 - molar fraction of CO2, in mol/mol
	 */
	public void setProperties(double temperature, double pressure, double relHumidity, double xCO2)
	{
		mTemperature = temperature;
		mPressure = pressure;
		mHumidity = relHumidity;
		m_xCO2 = xCO2;
	    double kelvin = 273.15d + mTemperature;
	    double pascal = 1000.0d * pressure;
		
	    // Enhancement factor, from CIPM 2007.
	    double enhancement = 1.00062d + 3.14e-5d*pressure + 5.6e-7d*mTemperature*mTemperature;
	    // Saturated vapour pressure, in kPa, from CIPM-2007.
	    double Psv = 0.001d * Math.exp(1.2378847e-5d*kelvin*kelvin - 1.9121316e-2d*kelvin
	    		+ 33.93711047d - 6.3431645e3d/kelvin);
	    // Molar fraction of water vapour, n_v/n_total, in mol/mol, using CIPM-2007.
	    m_xv = 0.01d * relHumidity * enhancement * Psv/pressure;
	    // Compressibility factor, from CIPM-2007,
	    double compressibility
	    		= (1.0d
	               - pascal/kelvin*(1.58123e-6d -2.9331e-8d*mTemperature
	            		+ 1.1043e-10d*mTemperature*mTemperature
	                    + (5.707e-6d - 2.051e-8d*mTemperature)*m_xv
	                    + (1.9898e-4d - 2.376e-6d*mTemperature)*m_xv*m_xv)
	               + (pascal/kelvin)*(pascal/kelvin)*(1.83e-11d - 0.765e-8d*m_xv*m_xv));
	    // Standard molar mass of dry air, in kg/kmol.
	    double Ma = Ma0 + (Mco2-Mo2)*xCO2;
	    // Standard molar mass of moist air, in kg/kmol.
	    double M = (1.0d-m_xv)*Ma + m_xv*Mv;
	    // Specific gas constant of humid air, in J/(kg*K).
	    double Ra = R/(0.001d*M);
	    // Specific humidity, or mass fraction of water vapour, in kg(water)/kg(total).
	    double qv = m_xv*Mv / M;
	    // Mass fraction of CO2, in kg(CO2)/kg(total).
	    double qco2 = xCO2*Mco2 / M;

	    mRho = pressure * 1e3d / (compressibility * Ra * kelvin);
	    
	    // Dynamic viscosity, in kg/(m.s) or Pa.s.
	    
	    // Dynamic viscosity of dry air, using Sutherland's formula,
	    // from McQuillan, et al., 1984 (Reid, 1966).
	    double etaAir = 1.4592e-6d * Math.pow(kelvin, 1.5d) / (kelvin + 109.10d);
        // Dynamic viscosity of water vapour in air,
        // linear regression line from Tsilingiris, 2007, corrected for magnitude.
	    double etaVapour = 8.058131868e-6d + mTemperature*4.000549451e-8d;
	    double etaRatio = Math.sqrt(etaAir/etaVapour);
	    double humidityRatio = m_xv/(1.0d-m_xv);
	    double phiAV = 0.5d*Math.pow(1.0d + etaRatio*Math.pow(Mv/Ma,0.25d),2.0d)
	    				/Math.sqrt(2.0d*(1.0d+(Ma/Mv)));
	    double phiVA = 0.5d*Math.pow(1.0d + Math.pow(Ma/Mv,0.25d)/etaRatio,2.0d)
	    				/Math.sqrt(2.0d*(1.0d+(Mv/Ma)));
	    mEta = etaAir/(1.0d + phiAV*humidityRatio)
	    			+ humidityRatio*etaVapour/(humidityRatio + phiVA);

	    // Isobaric specific heat, cp, in J/(kg.K).
	    
	    // Isobaric specific heat of air and water vapour, from Tsilingiris, 2007,
	    // with specific heat of air reduced by 2 J/kg.K to get gamma correct.
	    double cpAir = 1032.0d+kelvin*(-0.284887d+kelvin*(0.7816818e-3d+kelvin*(-0.4970786e-6d+kelvin*0.1077024e-9d)));
	    double cpVapour = 1869.10989d+mTemperature*(-0.2578421578d + mTemperature*1.941058941e-2d);
	    // Isobaric specific heat of CO2, curve fit on available data.
	    double cpCO2 = 817.02d + mTemperature*(1.0562d-mTemperature*6.67e-4d);
	    mSpecificHeat = cpAir*(1d-qv-qco2) + cpVapour*qv + cpCO2*qco2;
	    // Ratio of specific heats cp/cv.
	    mGamma = mSpecificHeat / (mSpecificHeat - Ra);

	    // Thermal conductivity, in W/(m.K).
	    
	    // Thermal conductivity of dry air, using Sutherland's formula, from McQuillan, et al., 1984.
	    double kappaAir = 2.3340e-3d * Math.pow(kelvin,1.5d)/ (kelvin + 164.54d);
	    // Thermal conductivity of water vapour, from Tsirilingis, 2007.
	    double kappaVapour = 0.01761758242d + mTemperature*(5.558941059e-5d + mTemperature*1.663336663e-7d);
	    mKappa = kappaAir/(1.0d + phiAV*humidityRatio)
    			+ humidityRatio*kappaVapour/(humidityRatio + phiVA);

	    // Prandtl number
	    mPrandtl = mEta * mSpecificHeat / mKappa;

		mSpeedOfSound = Math.sqrt(mGamma * compressibility * Ra * kelvin);
		
		mEpsilonConstant =  1.0d/(2.0d*Math.sqrt(Math.PI)) 
	            * Math.sqrt(mEta/mRho)
	            * (1.0d + (mGamma - 1.0d)/Math.sqrt(mPrandtl));
		mAlphaConstant = Math.sqrt(mEta / (2.0d * mRho * mSpeedOfSound))
				* (1.0d + (mGamma - 1.0d) / Math.sqrt(mPrandtl));

		mWaveNumber1 = 2.0d * Math.PI / mSpeedOfSound;
	} // setProperties
	
	/**
	 * Compute the actual air pressure, in kPa, at specified elevation,
     * from the barometric formula.
     * @param barometricPressure - pressure shown on barometer, adjusted to sea-level
	 * @param elevation - elevation in meters
	 * @return absolute air pressure, in kPa
	 */
	static public double pressureAt(double barometricPressure, double elevation)
	{
		// Concentration of CO2 in the atmosphere.
		double xCO2 = 0.000390d;
		// Standard molar mass of air, in kg/kmol.
		double Ma = Ma0 + (Mco2-Mo2)*xCO2;
		// Gravitational acceleration, in m/s^2
		double g = 9.80665d;
		return barometricPressure * Math.exp(- g * Ma * 0.001d * elevation / (R * 288.15d));
	}

	/**
	 * Compute the standard air pressure, in kPa, at specified elevation.
	 * @param elevation - elevation in meters
	 * @return standard air pressure, in kPa
	 */
	static public double pressureAt(double elevation)
	{
		return pressureAt(101.325d,elevation);
	}

	/* Calculate speed of sound using Owen Cramer's polynomial approximation,
	 * "The variation of the specific heat ratio and the speed of sound in air
	 * with temperature, pressure, humidity, and CO2 concentration,"
	 * JASA, 93 (5), 1993.
	 * This code assumes pressure of 101.0 kPa, and specific CO2 concentration.
	 */
	@SuppressWarnings("unused")
	private double calculateSpeedOfSound(double ambientTemp,
			double relativeHumidity)

	{
		double T;
		double f;
		double Psv;
		double Xw;
		double c;
		double Xc;
		double speed;
		double p = 101000d;
		double[] a = new double[] { 331.5024d, 0.603055d, -0.000528d, 51.471935d,
				0.1495874d, -0.000782d, -1.82e-7d, 3.73e-8d, -2.93e-10d, -85.20931d,
				-0.228525d, 5.91e-5d, -2.835149d, -2.15e-13d, 29.179762d, 0.000486d };

		T = ambientTemp + 273.15d;
		f = 1.00062d + 0.0000000314d * p + 0.00000056d * ambientTemp * ambientTemp;
		Psv = Math.exp(0.000012811805d * T * T - 0.019509874d * T + 34.04926034d
				- 6353.6311d / T);
		Xw = relativeHumidity * f * Psv / p;
		c = 331.45d - a[0] - p * a[6] - a[13] * p * p;
		c = Math.sqrt(a[9] * a[9] + 4d * a[14] * c);
		Xc = (-a[9] - c) / (2d * a[14]);

		speed = a[0]
				+ a[1]
				* ambientTemp
				+ a[2]
				* ambientTemp
				* ambientTemp
				+ (a[3] + a[4] * ambientTemp + a[5] * ambientTemp * ambientTemp)
				* Xw
				+ (a[6] + a[7] * ambientTemp + a[8] * ambientTemp * ambientTemp)
				* p
				+ (a[9] + a[10] * ambientTemp + a[11] * ambientTemp
						* ambientTemp) * Xc + a[12] * Xw * Xw + a[13] * p * p
				+ a[14] * Xc * Xc + a[15] * Xw * p * Xc;

		return speed;

	}

	/**
	 * Calculate the wave impedance, in kg/(m^4.s), of a bore of nominal radius r.
	 */
	public double calcZ0(double radius)
	{
		return mRho * mSpeedOfSound / (Math.PI * radius * radius);
		// Wave impedance of a bore, nominal radius r.
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Physical Parameters :\n");
		buf.append("Temperature = " + mTemperature + "\n");
		buf.append("Pressure = " + mPressure + "\n");
		buf.append("c = " + mSpeedOfSound + "\n");
		buf.append("rho = " + mRho + "\n");
		buf.append("Specific Heat = " + mSpecificHeat + "\n");
		buf.append("eta = " + mEta + "\n");
		buf.append("gamma = " + mGamma + "\n");
		buf.append("kappa = " + mKappa + "\n");
		buf.append("nu**2 = " + mPrandtl + "\n");

		return buf.toString();
	}
	
	public void printProperties()
	{
		System.out.print("Properties of air at ");
		System.out.printf("%6.2f C, %8.3f kPa, %3.0f%% humidity, %3.0f ppm CO2:\n",
				mTemperature, mPressure, mHumidity, m_xCO2*1.0e6d);
		System.out.printf("Speed of sound is %8.3f m/s.\n", mSpeedOfSound);
		System.out.printf("Density is %7.4f kg/m^3.\n", mRho);
		System.out.printf("Epsilon factor is %9.3e.\n", mEpsilonConstant);
	}

	/**
	 * @return temperature, in Celsius
	 */
	public double getTemperature()
	{
		return mTemperature;
	}

	/**
	 * @return air pressure, in kPa
	 */
	public double getPressure()
	{
		return mPressure;
	}

	/**
	 * @return molar fraction of CO2 in air, in mol/mol
	 */
	public double get_xCO2()
	{
		return m_xCO2;
	}

	/**
	 * @return molar fraction of water vapour in air, in mol/mol
	 */
	public double get_xv()
	{
		return m_xv;
	}

	/**
	 * @return the speed of sound, in m/s.
	 */
	public double getSpeedOfSound()
	{
		return mSpeedOfSound;
	}

	/**
	 * Convert frequency to wave number.
	 * @param freq : frequency in Hz.
	 * @return wave number in radians/meter.
	 */
	public double calcWaveNumber(double freq)
	{
		return freq * mWaveNumber1;
	}

	/**
	 * Convert wave number to frequency.
	 * @param waveNumber : wave number in radians/meter
	 * @return frequency in Hz.
	 */
	public double calcFrequency(double waveNumber)
	{
		return waveNumber / mWaveNumber1;
	}

	/**
	 * Compute epsilon, the adjustment factor for losses in a tube.
	 * @param waveNumber : non-lossy wave number, in radians/meter
	 * @param radius : tube radius, in m
	 * @return dimensionless adjustment for calculating complex wave number
	 */
	public double getEpsilon(double waveNumber, double radius)
	{
		return mAlphaConstant / (radius * Math.sqrt(waveNumber));
	}
	/**
	 * Compute epsilon, the adjustment factor for losses in a tube.
	 * @param frequency : frequency, in Hz
	 * @param radius : tube radius, in m
	 * @return dimensionless adjustment for calculating complex wave number
	 */
	public double getEpsilonFromF(double frequency, double radius)
	{
	    return mEpsilonConstant / (radius * Math.sqrt(frequency));
	}

	/**
	 * Compute the complex wave vector, allowing for losses.
	 * @param waveNumber : non-lossy wave number, in radians/meter
	 * @param radius : tube radius, in m
	 * @return omega/v - j * alpha
	 */
	public Complex getComplexWaveNumber(double waveNumber, double radius)
	{
		double alpha = (1d / radius) * Math.sqrt(waveNumber)
				* mAlphaConstant;
		return Complex.I.multiply(waveNumber).add(
				Complex.valueOf(1d, 1d).multiply(alpha));
	}

	public double getAlphaConstant()
	{
		return mAlphaConstant;
	}

	/**
	 * @return specific heat at constant pressure, in J/(kg.K) 
	 */
	public double getSpecificHeat()
	{
		return mSpecificHeat;
	}
	/**
	 * @return specific heat at constant pressure, in J/(kg.K) 
	 */
	public double getC_p()
	{
		return mSpecificHeat;
	}

	/**
	 * @return the dimensionless specific heat ratio, cp/cv
	 */
	public double getSpecificHeatRatio()
	{
		return mGamma;
	}

	/**
	 * @return the dimensionless specific heat ratio, cp/cv
	 */
	public double getGamma()
	{
		return mGamma;
	}

	/**
	 * @return the dynamic viscosity, in Kg/(m.s) or Pa.s
	 */
	public double getDynamicViscosity()
	{
		return mEta;
	}
	/**
	 * @return the dynamic viscosity, in Kg/(m.s) or Pa.s
	 */
	public double getEta()
	{
		return mEta;
	}

	/**
	 * @return the air density, in kg/m^3
	 */
	public double getDensity()
	{
		return mRho;
	}

	/**
	 * @return the air density, in kg/m^3
	 */
	public double getRho()
	{
		return mRho;
	}

	/**
	 * @return Thermal conductivity, in W/(m.K)
	 */
	public double getThermalConductivity()
	{
		return mKappa;
	}
	/**
	 * @return Thermal conductivity, in W/(m.K)
	 */
	public double getKappa()
	{
		return mKappa;
	}
}
