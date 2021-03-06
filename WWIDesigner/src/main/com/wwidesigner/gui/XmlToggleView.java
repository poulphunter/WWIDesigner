package com.wwidesigner.gui;

import java.awt.Dimension;

import com.jidesoft.app.framework.BasicDataModel;
import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.DataModelException;
import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.gui.DataViewPane;

/**
 * Data view associated with an XML String DataModel. Data is presented in
 * either an XML editor pane or a more user-friendly view specific to the data
 * type represented by the XML data. The choice may be made as: hard-wired
 * defaults, configurable defaults, or a menu toggle.
 * 
 * <p/>
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Edward N. Kort
 */
public class XmlToggleView extends DataViewPane
{
	private ContainedXmlView currentView;
	private StudyModel studyModel;

	@Override
	protected void initializeComponents()
	{
		// sets the window size
		setPreferredSize(new Dimension(550, 400));
		setBorder(null);

		studyModel = ((WIDesigner) getApplication()).getStudyView()
				.getStudyModel();
	}

	@Override
	public void updateView(DataModel dataModel) throws DataModelException
	{
		if (dataModel instanceof FileDataModel)
		{
			FileDataModel model = (FileDataModel) dataModel;
			// Band-aid for intermittent bogus exception on Mac
			try
			{
				currentView = studyModel.getDefaultXmlView(model, this);
			}
			catch (NullPointerException ex)
			{
				return;
			}
			String xmlData = model.getData().toString();
			currentView.setText(xmlData);
			removeAll();
			add(currentView.getViewComponent());
		}
	}

	@Override
	public void updateModel(DataModel dataModel) throws DataModelException
	{
		if (currentView == null)
		{
			return;
		}
		if (dataModel instanceof BasicDataModel)
		{
			String text = currentView.getText();
			// Empty text is never appropriate for XML, and probably indicates
			// a data view that has not yet been loaded.
			if (text != null && !text.isEmpty())
			{
				((BasicDataModel) dataModel).setData(text);
			}
		}
	}

	public void toggleView() throws DataModelException
	{
		BasicDataModel dataModel = (BasicDataModel) getApplication()
				.getDataModel(this);
		ContainedXmlView nextView = studyModel.getNextXmlView(dataModel,
				currentView, this);
		nextView.setText(currentView.getText());
		removeAll();
		add(nextView.getViewComponent());
		currentView = nextView;
		updateUI();
	}

	public String getText() throws DataModelException
	{
		return currentView.getText();
	}

	public ContainedXmlView getCurrentView()
	{
		return currentView;
	}
}
