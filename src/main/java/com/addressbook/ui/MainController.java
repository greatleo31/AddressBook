package com.addressbook.ui;

import com.addressbook.model.ContactViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {

  @FXML
  private TreeView<String> groupTreeView;

  @FXML
  private TableView<ContactViewModel> contactTableView;

  @FXML
  private TableColumn<ContactViewModel, String> nameColumn;

  @FXML
  private TableColumn<ContactViewModel, String> mobileColumn;

  @FXML
  private TableColumn<ContactViewModel, String> emailColumn;

  @FXML
  private TableColumn<ContactViewModel, String> groupColumn;

  @FXML
  private TextField searchField;

  @FXML
  public void initialize() {
    // 1. 初始化左侧分组区 (静态数据)
    TreeItem<String> rootItem = new TreeItem<>("所有联系人");
    rootItem.setExpanded(true);

    TreeItem<String> unassignedItem = new TreeItem<>("未分组联系人");

    TreeItem<String> groupsRoot = new TreeItem<>("联系组");
    groupsRoot.setExpanded(true);
    groupsRoot.getChildren().addAll(
        new TreeItem<>("家人"),
        new TreeItem<>("朋友"),
        new TreeItem<>("同事"));

    rootItem.getChildren().addAll(unassignedItem, groupsRoot);
    groupTreeView.setRoot(rootItem);

    // 2. 初始化右侧联系人列表 (静态数据)
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobile"));
    emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    groupColumn.setCellValueFactory(new PropertyValueFactory<>("group"));

    ObservableList<ContactViewModel> dummyData = FXCollections.observableArrayList(
        new ContactViewModel("张三", "13800138000", "zhangsan@example.com", "同事"),
        new ContactViewModel("李四", "13900139000", "lisi@example.com", "朋友"),
        new ContactViewModel("王小明", "13700137000", "wxm@test.com", "家人"),
        new ContactViewModel("赵六", "13600136000", "zhaoliu@demo.com", "同事"));
    contactTableView.setItems(dummyData);
  }

  @FXML
  private void onAddContact() {
    System.out.println("点击了新建联系人");
  }

  @FXML
  private void onEditContact() {
    System.out.println("点击了编辑联系人");
  }

  @FXML
  private void onDeleteContact() {
    System.out.println("点击了删除联系人");
  }

  @FXML
  private void onImportExport() {
    System.out.println("点击了导入/导出");
  }
}
