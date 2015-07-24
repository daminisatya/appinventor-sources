// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.component.ComponentInfo;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Command;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

public class ComponentImportWizard extends Wizard {

  private static class ImportComponentCallback extends OdeAsyncCallback<Boolean> {
    @Override
    public void onSuccess(Boolean result) {
      // to be implemented
    }
  }

  private static int MY_COMPONENT_TAB = 0;
  private static int URL_TAB = 1;

  private final Ode ode = Ode.getInstance();

  public ComponentImportWizard() {
    super(MESSAGES.componentImportWizardCaption(), true, false);

    final CellTable compTable = createCompTable();
    final Grid urlGrid = createUrlGrid();
    final TabPanel tabPanel = new TabPanel();
    tabPanel.add(compTable, "My components");
    tabPanel.add(urlGrid, "URL");
    tabPanel.selectTab(MY_COMPONENT_TAB);

    VerticalPanel panel = new VerticalPanel();
    panel.add(tabPanel);

    addPage(panel);

    setPagePanelHeight(400);
    setPixelSize(400, 400);
    setStylePrimaryName("ode-DialogBox");

    ListDataProvider<ComponentInfo> dataProvider = provideData();
    dataProvider.addDataDisplay(compTable);

    initFinishCommand(new Command() {
      @Override
      public void execute() {
        final long projectId = ode.getCurrentYoungAndroidProjectId();
        final Project project = ode.getProjectManager().getProject(projectId);
        final YoungAndroidAssetsFolder assetsFolderNode =
            ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();

        if (tabPanel.getTabBar().getSelectedTab() == MY_COMPONENT_TAB) {
          SingleSelectionModel<ComponentInfo> selectionModel =
              (SingleSelectionModel<ComponentInfo>) compTable.getSelectionModel();
          ComponentInfo toImport = selectionModel.getSelectedObject();

          if (toImport == null) {
            showAlert(MESSAGES.noComponentSelectedError());
            return;
          }

          ode.getComponentService().importComponentToProject(toImport, projectId,
              assetsFolderNode.getFileId(), new ImportComponentCallback());

        } else if (tabPanel.getTabBar().getSelectedTab() == URL_TAB) {
          TextBox urlTextBox = (TextBox) urlGrid.getWidget(1, 0);
          String url = urlTextBox.getText();

          if (url.trim().isEmpty()) {
            showAlert(MESSAGES.noUrlError());
            return;
          }

          ode.getComponentService().importComponentToProject(url, projectId,
              assetsFolderNode.getFileId(), new ImportComponentCallback());
        }
      }
    });
  }

  private CellTable createCompTable() {
    final SingleSelectionModel<ComponentInfo> selectionModel =
        new SingleSelectionModel<ComponentInfo>();

    CellTable<ComponentInfo> compTable = new CellTable<ComponentInfo>();
    compTable.setSelectionModel(selectionModel);

    Column<ComponentInfo, Boolean> checkColumn =
        new Column<ComponentInfo, Boolean>(new CheckboxCell(true, false)) {
          @Override
          public Boolean getValue(ComponentInfo object) {
            return selectionModel.isSelected(object);
          }
        };
    Column<ComponentInfo, String> nameColumn =
        new Column<ComponentInfo, String>(new TextCell()) {
          @Override
          public String getValue(ComponentInfo compInfo) {
            return compInfo.getName();
          }
        };
    Column<ComponentInfo, Number> versionColumn =
        new Column<ComponentInfo, Number>(new NumberCell()) {
          @Override
          public Number getValue(ComponentInfo compInfo) {
            return compInfo.getVersion();
          }
        };

    compTable.addColumn(checkColumn);
    compTable.addColumn(nameColumn, "Component");
    compTable.addColumn(versionColumn, "Version");

    return compTable;
  }

  private Grid createUrlGrid() {
    Grid grid = new Grid(2, 1);
    grid.setWidget(0, 0, new Label("Url:"));
    grid.setWidget(1, 0, new TextBox());
    return grid;
  }

  private ListDataProvider<ComponentInfo> provideData() {
    ListDataProvider<ComponentInfo> provider = new ListDataProvider<ComponentInfo>();
    for (ComponentInfo compInfo : ode.getComponentManager().getRetrivedComponentInfos()) {
      provider.getList().add(compInfo);
    }
    return provider;
  }

  private void showAlert(String message) {
    Window.alert(message);
    center();
  }
}
