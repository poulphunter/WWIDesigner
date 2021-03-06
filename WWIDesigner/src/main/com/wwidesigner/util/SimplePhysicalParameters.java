/**
 * Simplified model of the physical properties of air.
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

import static com.wwidesigner.util.Constants.*;

/**
 * Simplified version of the physical parameters class. Only supports varying
 * temperature and relative humidity. Used exclusively in the
 * DefaultFippleMouthpieceCalculator, the calculator used for the NAF.
 * 
 * @author Edward Kort, Burton Patkau
 */
public class SimplePhysicalParameters
{
	private double mRho;
	private double mEta;
	private double mTemperature; // Temperature, in Celsius.
	private double mSpecificHeat;
	private double mSpeedOfSound; // in metres per second.

	private double mMu;
	private double mGamma;
	private double mNu;
	private double mWaveNumber1; // Wave number, k, at 1 Hz: 2*pi/c, in radians
									// per metre.
	private double mAlphaConstant;

	private static final double mRelativeHumidity = 0.45;

	public SimplePhysicalParameters()
	{
		this(72.0, TemperatureType.F);
	}

	public SimplePhysicalParameters(PhysicalParameters params)
	{
		// PhysicalParameters always store temperature in Celcius.
		this(params.getTemperature(), TemperatureType.C);
	}

	public SimplePhysicalParameters(double temperature, TemperatureType tempType)
	{
		switch (tempType)
		{
			case C:
				mTemperature = temperature;
				break;
			case F:
				mTemperature = (temperature + 40.) * 5. / 9. - 40.;
				break;
		}
		mSpeedOfSound = calculateSpeedOfSound(mTemperature, mRelativeHumidity);

		mEta = 3.648e-6 * (1 + 0.0135003 * (mTemperature + 273.15));

		double deltaT = mTemperature - 26.85;

		mRho = 1.1769 * (1 - 0.00335 * deltaT);
		mMu = 1.8460E-5 * (1 + 0.00250 * deltaT);
		mGamma = 1.4017 * (1 - 0.00002 * deltaT);
		mNu = 0.8410 * (1 - 0.00020 * deltaT);
		mWaveNumber1 = 2.0 * Math.PI / mSpeedOfSound;

		mAlphaConstant = Math.sqrt(mMu / (2 * mRho * mSpeedOfSound))
				* (1 + (mGamma - 1) / mNu);

	}

	/**
	 * Calculations from publication by Yang Yili
	 */
	public double calculateSpeedOfSound(double ambientTemp,
			double relativeHumidity)
	{
		double T;
		double f;
		double Psv;
		double Xw;
		double c;
		double Xc;
		double speed;
		double p = 101000;
		double[] a = new double[] { 331.5024, 0.603055, -0.000528, 51.471935,
				0.1495874, -0.000782, -1.82e-7, 3.73e-8, -2.93e-10, -85.20931,
				-0.228525, 5.91e-5, -2.835149, -2.15e-13, 29.179762, 0.000486 };

		T = ambientTemp + 273.15;
		f = 1.00062 + 0.0000000314 * p + 0.00000056 * ambientTemp * ambientTemp;
		Psv = Math.exp(0.000012811805 * T * T - 0.019509874 * T + 34.04926034
				- 6353.6311 / T);
		Xw = relativeHumidity * f * Psv / p;
		c = 331.45 - a[0] - p * a[6] - a[13] * p * p;
		c = Math.sqrt(a[9] * a[9] + 4 * a[14] * c);
		Xc = ((-1) * a[9] - c) / (2 * a[14]);

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

	public double getGamma()
	{
		return mGamma;
	}

	/**
	 * Calculate the wave impedance of a bore of nominal radius r, given these
	 * parameters.
	 */
	public double calcZ0(double radius)
	{
		return mRho * mSpeedOfSound / (Math.PI * radius * radius);
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Physical Parameters :\n");
		buf.append("Temperature = " + mTemperature + "\n");
		buf.append("Specific Heat = " + mSpecificHeat + "\n");
		buf.append("rho = " + mRho + "\n");
		buf.append("c = " + mSpeedOfSound + "\n");
		buf.append("eta = " + mEta + "\n");
		buf.append("gamma = " + GAMMA + "\n");
		buf.append("kappa = " + KAPPA + "\n");
		buf.append("C_p = " + C_P + "\n");
		buf.append("nu = " + NU + "\n");

		return buf.toString();
	}

	/**
	 * @return the eta
	 */
	public double getEta()
	{
		return mEta;
	}

	/**
	 * @return the rho
	 */
	public double getRho()
	{
		return mRho;
	}

	/**
	 * @return the specificHeat
	 */
	public double getSpecificHeat()
	{
		return mSpecificHeat;
	}

	/**
	 * @return the temperature, in Celsius.
	 */
	public double getTemperature()
	{
		return mTemperature;
	}

	/**
	 * 
	 * @return the speed of sound, in m/s.
	 */
	public double getSpeedOfSound()
	{
		return mSpeedOfSound;
	}

	public double getAlphaConstant()
	{
		return mAlphaConstant;
	}

	/**
	 * 
	 * @param freq
	 *            : frequency in Hz.
	 * @return wave number in radians/metre.
	 */
	public double calcWaveNumber(double freq)
	{
		return freq * mWaveNumber1;
	}

	/**
	 * 
	 * @param wave
	 *            number in radians/metre.
	 * @return frequency in Hz.
	 */
	public double calcFrequency(double waveNumber)
	{
		return waveNumber / mWaveNumber1;
	}
}
