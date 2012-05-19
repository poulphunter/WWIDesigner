/**
 * 
 */
package com.wwidesigner.note;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.wwidesigner.util.Constants;

/**
 * @author kort
 * 
 */
public class TemperamentTest
{

    private Temperament temperament;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        temperament = Temperament.getEqualTemperament();
    }

    /**
     * Test method for
     * {@link com.wwidesigner.note.Temperament#nearestNote(double, double)}.
     */
    @Test
    public void testNearestNote()
    {
        double frequency = Constants.A440;
        double referenceFreq = Constants.A440;
        DeviatedNominalNote note = temperament.nearestNote( frequency, referenceFreq );
        assertEquals( 0, note.getCentsDeviation(), 0.001 );

        frequency *= 4.;
        note = temperament.nearestNote( frequency, referenceFreq );
        assertEquals( 0, note.getCentsDeviation(), 0.001 );
        assertEquals( 2, note.getOctave() );
    }

    /**
     * Test method for
     * {@link com.wwidesigner.note.Temperament#getFreq(com.wwidesigner.note.NominalNote, double)}.
     */
    @Test
    public void testGetFreq()
    {
        NominalNote note = new NominalNote( "A", 4 );
        double frequency = temperament.getFreq( note, Constants.A440, note );
        assertEquals( 440, frequency, 0.001 );

        note = new NominalNote( "A", 3 );
        frequency = temperament.getFreq( note, Constants.A440, note );
        assertEquals( 220, frequency, 0.001 );
    }

}
