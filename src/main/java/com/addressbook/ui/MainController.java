package com.addressbook.ui;

import com.addressbook.model.ContactViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class MainController {
  private ObservableList<ContactViewModel> masterData = FXCollections.observableArrayList();

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
  @SuppressWarnings("unchecked")
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

    masterData.addAll(
        new ContactViewModel("张三", "13800138000", "zhangsan@example.com", "同事"),
        new ContactViewModel("李四", "13900139000", "lisi@example.com", "朋友"),
        new ContactViewModel("王小明", "13700137000", "wxm@test.com", "家人"),
        new ContactViewModel("赵六", "13600136000", "zhaoliu@demo.com", "同事"));

    FilteredList<ContactViewModel> filteredData = new FilteredList<>(masterData, p -> true);
    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      filteredData.setPredicate(contact -> {
        if (newValue == null || newValue.isEmpty())
          return true;
        String lowerCaseFilter = newValue.toLowerCase();
        if (contact.getName().toLowerCase().contains(lowerCaseFilter))
          return true;
        if (contact.getMobile().toLowerCase().contains(lowerCaseFilter))
          return true;
        if (contact.getGroup().toLowerCase().contains(lowerCaseFilter))
          return true;
        return false;
      });
    });

    SortedList<ContactViewModel> sortedData = new SortedList<>(filteredData);
    sortedData.comparatorProperty().bind(contactTableView.comparatorProperty());
    contactTableView.setItems(sortedData);
  }

  @FXML
  private void onAddContact() {
    Dialog<ContactViewModel> dialog = createContactDialog("新建联系人", null);
    Optional<ContactViewModel> result = dialog.showAndWait();
    result.ifPresent(contact -> masterData.add(contact));
  }

  @FXML
  private void onEditContact() {
    ContactViewModel selected = contactTableView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      Dialog<ContactViewModel> dialog = createContactDialog("编辑联系人", selected);
      Optional<ContactViewModel> result = dialog.showAndWait();
      result.ifPresent(updatedContact -> {
        int index = masterData.indexOf(selected);
        masterData.set(index, updatedContact);
      });
    }
  }

  @FXML
  private void onDeleteContact() {
    ContactViewModel selected = contactTableView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      masterData.remove(selected);
    }
  }

  @FXML
  private void onImportExport() {
    System.out.println("点击了导入/导出");
  }

  private Dialog<ContactViewModel> createContactDialog(String title, ContactViewModel existing) {
    Dialog<ContactViewModel> dialog = new Dialog<>();
    dialog.setTitle(title);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField nameField = new TextField();
    TextField mobileField = new TextField();
    TextField emailField = new TextField();
    TextField groupField = new TextField();

    if (existing != null) {
      nameField.setText(existing.getName());
      mobileField.setText(existing.getMobile());
      emailField.setText(existing.getEmail());
      groupField.setText(existing.getGroup());
    }

    grid.add(new Label("姓名:"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("手机:"), 0, 1);
    grid.add(mobileField, 1, 1);
    grid.add(new Label("邮箱:"), 0, 2);
    grid.add(emailField, 1, 2);
    grid.add(new Label("分组:"), 0, 3);
    grid.add(groupField, 1, 3);

    dialog.getDialogPane().setContent(grid);
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return new ContactViewModel(nameField.getText(), mobileField.getText(), emailField.getText(),
            groupField.getText());
      }
      return null;
    });
    return dialog;
  }
}
