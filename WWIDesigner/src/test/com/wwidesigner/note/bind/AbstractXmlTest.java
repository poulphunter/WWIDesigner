package com.wwidesigner.note.bind;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractXmlTest<T>
{
	public abstract void setVariables();

	String inputSymbolXML;
	String outputSymbolXML;

	File inputFile;
	File outputFile;
	String writePath;

	T inputElement;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		setVariables();
		NoteBindFactory bindFactory = new NoteBindFactory();
		String inputFilePath = bindFactory.getPathFromName(inputSymbolXML);
		inputFile = new File(inputFilePath);
		String inputFileName = inputFile.getName();
		writePath = inputFilePath.substring(0,
				inputFilePath.indexOf(inputFileName));

		// Delete output file
		outputFile = new File(writePath + outputSymbolXML);
		outputFile.delete();
	}

	/**
	 * Unmarshal the input XML, and include validation
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void unmarshalInput() throws Exception
	{
		NoteBindFactory bindFactory = new NoteBindFactory();
		inputElement = (T) bindFactory.unmarshalXml(inputFile);
	}

	/**
	 * Marshal the input element
	 * @throws Exception
	 */
	public void marshalOutput() throws Exception
	{
		NoteBindFactory bindFactory = new NoteBindFactory();
		bindFactory.marshalToXml(inputElement, outputFile);
	}

	/**
	 * Test method for round-trip marshalling/unmarshalling
	 */
	@Test
	public final void testUnmarshalMarshal()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			marshalOutput();
			XmlDiff diff = new XmlDiff(inputFile, outputFile);
			assertTrue(diff.toString(), diff.identical());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

}
