package com.addressbook.ui;

import com.addressbook.model.Contact;
import com.addressbook.model.ContactViewModel;
import com.addressbook.service.ContactManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class MainController {
  private ObservableList<ContactViewModel> masterData = FXCollections.observableArrayList();
  private ContactManager contactManager;

  @FXML
  private TreeView<String> groupTreeView;
  @FXML
  private ListView<ContactViewModel> contactListView;
  @FXML
  private TextField searchField;

  @FXML
  @SuppressWarnings("unchecked")
  public void initialize() {
    contactManager = new ContactManager();

    // 1. 设置 ListView 多选与渲染工厂
    contactListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    contactListView.setCellFactory(listView -> new ContactListCell());

    refreshGroupTree();

    // 3. 加载数据并初始化可观察集合
    List<Contact> contacts = contactManager.getAllContacts();
    for (Contact c : contacts) {
      masterData.add(new ContactViewModel(c));
    }

    // 4. 定义综合搜索与分组过滤机制
    FilteredList<ContactViewModel> filteredData = new FilteredList<>(masterData, p -> true);

    // 5. 将过滤后的数据包裹进 SortedList 进行拼音/首字母字典序排序
    SortedList<ContactViewModel> sortedData = new SortedList<>(filteredData, (a, b) -> {
      // null 或者为空做兜底处理
      String pinyinA = a.getFullPinyin() == null ? "" : a.getFullPinyin();
      String pinyinB = b.getFullPinyin() == null ? "" : b.getFullPinyin();
      return pinyinA.compareToIgnoreCase(pinyinB);
    });

    // 添加监听：输入框变化 或 左侧树选择变化 时均重新评估过滤条件
    searchField.textProperty().addListener((observable, oldValue, newValue) -> filterData(filteredData));
    groupTreeView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> filterData(filteredData));

    contactListView.setItems(sortedData);

    // 初始化列表元素的右键菜单 (分配修改分组等)
    setupListViewContextMenu();
  }

  private void filterData(FilteredList<ContactViewModel> filteredData) {
    String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
    TreeItem<String> selectedGroupItem = groupTreeView.getSelectionModel().getSelectedItem();

    String filterGroup = null;
    if (selectedGroupItem != null && selectedGroupItem.getParent() != null) {
      // 不是根节点 "所有联系人"
      if ("未分组联系人".equals(selectedGroupItem.getValue())) {
        filterGroup = ""; // 过滤出空分组的
      } else if (selectedGroupItem.getParent().getValue().equals("联系组")) {
        filterGroup = selectedGroupItem.getValue();
      }
    }

    final String currentTargetGroup = filterGroup;

    filteredData.setPredicate(contact -> {
      // 分组过滤
      boolean groupMatch = true;
      if (currentTargetGroup != null) {
        if (currentTargetGroup.isEmpty()) {
          groupMatch = (contact.getGroup() == null || contact.getGroup().trim().isEmpty());
        } else {
          groupMatch = currentTargetGroup.equals(contact.getGroup());
        }
      }

      if (!groupMatch)
        return false;

      // 文本搜索过滤
      if (searchText.isEmpty())
        return true;

      String lowerSearch = searchText.toLowerCase();

      return (contact.getName() != null && contact.getName().toLowerCase().contains(lowerSearch)) ||
          (contact.getMobile() != null && contact.getMobile().toLowerCase().contains(lowerSearch)) ||
          (contact.getPhone() != null && contact.getPhone().toLowerCase().contains(lowerSearch)) ||
          (contact.getFullPinyin() != null && contact.getFullPinyin().toLowerCase().contains(lowerSearch)) ||
          (contact.getFirstLetter() != null && contact.getFirstLetter().toLowerCase().contains(lowerSearch));
    });
  }

  // 刷新左侧树状结构并设置右键增删功能
  private void refreshGroupTree() {
    TreeItem<String> rootItem = new TreeItem<>("所有联系人");
    rootItem.setExpanded(true);
    TreeItem<String> unassignedItem = new TreeItem<>("未分组联系人");
    TreeItem<String> groupsRoot = new TreeItem<>("联系组");
    groupsRoot.setExpanded(true);

    // 提取动态分组（通过现有联系人的实际分组，同时保留几个默认标签）
    java.util.Set<String> groups = new java.util.HashSet<>();
    groups.add("家人");
    groups.add("朋友");
    groups.add("同事"); // 默认
    if (masterData != null) {
      for (ContactViewModel vm : masterData) {
        if (vm.getGroup() != null && !vm.getGroup().trim().isEmpty()) {
          groups.add(vm.getGroup().trim());
        }
      }
    }

    for (String g : groups) {
      groupsRoot.getChildren().add(new TreeItem<>(g));
    }

    rootItem.getChildren().addAll(unassignedItem, groupsRoot);
    groupTreeView.setRoot(rootItem);

    // 配置 TreeView 项作为拖放目标来接收拖拽的联系人，并动态生成右键菜单
    groupTreeView.setCellFactory(tv -> {
      TreeCell<String> cell = new TreeCell<String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
          super.updateItem(item, empty);
          setText(empty ? null : item);
          if (empty || item == null) {
            setContextMenu(null);
          } else {
            if ("联系组".equals(item)) {
              ContextMenu contextMenu = new ContextMenu();
              MenuItem addGroupItem = new MenuItem("新建分组");
              addGroupItem.setOnAction(e -> handleAddGroup());
              contextMenu.getItems().add(addGroupItem);
              setContextMenu(contextMenu);
            } else if (getTreeItem() != null && getTreeItem().getParent() != null
                && "联系组".equals(getTreeItem().getParent().getValue())) {
              ContextMenu contextMenu = new ContextMenu();
              MenuItem deleteGroupItem = new MenuItem("删除所选分组");
              deleteGroupItem.setOnAction(e -> {
                groupTreeView.getSelectionModel().select(getTreeItem());
                handleDeleteGroup();
              });
              contextMenu.getItems().add(deleteGroupItem);
              setContextMenu(contextMenu);
            } else {
              setContextMenu(null);
            }
          }
        }
      };

      cell.setOnDragOver(event -> {
        if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
          TreeItem<String> treeItem = cell.getTreeItem();
          if (treeItem != null) {
            if ("未分组联系人".equals(treeItem.getValue()) ||
                (treeItem.getParent() != null && "联系组".equals(treeItem.getParent().getValue()))) {
              event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
            }
          }
        }
        event.consume();
      });

      cell.setOnDragEntered(event -> {
        if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
          TreeItem<String> treeItem = cell.getTreeItem();
          if (treeItem != null && ("未分组联系人".equals(treeItem.getValue()) ||
              (treeItem.getParent() != null && "联系组".equals(treeItem.getParent().getValue())))) {
            cell.setStyle("-fx-background-color: #d0e8ff;");
          }
        }
      });

      cell.setOnDragExited(event -> {
        cell.setStyle("");
      });

      cell.setOnDragDropped(event -> {
        boolean success = false;
        if (event.getDragboard().hasString()) {
          String targetGroup = cell.getTreeItem().getValue();
          if ("未分组联系人".equals(targetGroup))
            targetGroup = "";

          // 本地拖放直接取多选数据
          assignSelectedContactsToGroup(targetGroup);
          success = true;
        }
        event.setDropCompleted(success);
        event.consume();
      });

      return cell;
    });
  }

  private void handleAddGroup() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("新建分组");
    dialog.setHeaderText("请输入新分组名称");
    dialog.setContentText("分组名:");
    Optional<String> result = dialog.showAndWait();
    result.ifPresent(name -> {
      if (!name.trim().isEmpty()) {
        // 找到 联系组 root
        TreeItem<String> contactGroupsTarget = groupTreeView.getRoot().getChildren().get(1);
        // 检查防重复
        boolean exists = contactGroupsTarget.getChildren().stream().anyMatch(t -> t.getValue().equals(name.trim()));
        if (!exists) {
          contactGroupsTarget.getChildren().add(new TreeItem<>(name.trim()));
        }
      }
    });
  }

  private void handleDeleteGroup() {
    TreeItem<String> selected = groupTreeView.getSelectionModel().getSelectedItem();
    if (selected != null && selected.getParent() != null && selected.getParent().getValue().equals("联系组")) {
      // 只允许删除 联系组 下的具体分组节点
      String groupName = selected.getValue();
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要删除分组 [" + groupName + "] 吗？\n该分组下的联系人不会被删除，它们将被移至未分组。");
      alert.setTitle("确认删除分组");
      Optional<ButtonType> res = alert.showAndWait();
      if (res.isPresent() && res.get() == ButtonType.OK) {
        selected.getParent().getChildren().remove(selected);
        // 批量更新属于该分组的联系人
        for (ContactViewModel vm : masterData) {
          if (groupName.equals(vm.getGroup())) {
            vm.setGroup("");
            contactManager.updateContact(vm.toContact());
          }
        }
        contactListView.refresh();
      }
    } else {
      Alert a = new Alert(Alert.AlertType.WARNING, "请选择一个具体的自定义分组来删除。");
      a.showAndWait();
    }
  }

  private void setupListViewContextMenu() {
    // 动态生成列表项级别的右键菜单
    ContextMenu listContextMenu = new ContextMenu();

    Menu addToGroupMenu = new Menu("移动至分组...");
    listContextMenu.getItems().add(addToGroupMenu);

    MenuItem deleteContact = new MenuItem("删除");
    deleteContact.setOnAction(evt -> onDeleteContact());
    listContextMenu.getItems().add(deleteContact);

    // 在右键菜单弹出前，动态填充子菜单 (读取当前全部可用分组)
    listContextMenu.setOnShowing(e -> {
      addToGroupMenu.getItems().clear();
      TreeItem<String> groupsRoot = groupTreeView.getRoot().getChildren().get(1);
      for (TreeItem<String> groupItem : groupsRoot.getChildren()) {
        MenuItem item = new MenuItem(groupItem.getValue());
        item.setOnAction(evt -> assignSelectedContactsToGroup(groupItem.getValue()));
        addToGroupMenu.getItems().add(item);
      }
      MenuItem removeGroup = new MenuItem(">> 移除分组 (移至未分组)");
      removeGroup.setOnAction(evt -> assignSelectedContactsToGroup(""));
      addToGroupMenu.getItems().add(new SeparatorMenuItem());
      addToGroupMenu.getItems().add(removeGroup);

      deleteContact.setDisable(contactListView.getSelectionModel().isEmpty());
    });
    // 将菜单附着到整个ListView
    contactListView.setContextMenu(listContextMenu);
  }

  private void assignSelectedContactsToGroup(String newGroupName) {
    List<ContactViewModel> selectedItems = FXCollections
        .observableArrayList(contactListView.getSelectionModel().getSelectedItems());
    if (selectedItems.isEmpty())
      return;

    for (ContactViewModel vm : selectedItems) {
      vm.setGroup(newGroupName);
      contactManager.updateContact(vm.toContact());
    }
    contactListView.refresh();
    // 重新过滤一次刷新视图效果
    searchField.setText(searchField.getText() + " ");
    searchField.setText(searchField.getText().trim());
  }

  @FXML
  private void onAddContact() {
    Dialog<ContactViewModel> dialog = createContactDialog("新建联系人", null);
    Optional<ContactViewModel> result = dialog.showAndWait();
    result.ifPresent(contactVm -> {
      Contact newContact = contactVm.toContact();
      contactManager.addContact(newContact);
      masterData.add(new ContactViewModel(newContact));
    });
  }

  @FXML
  private void onEditContact() {
    ContactViewModel selected = contactListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      Dialog<ContactViewModel> dialog = createContactDialog("编辑联系人", selected);
      Optional<ContactViewModel> result = dialog.showAndWait();
      result.ifPresent(updatedVm -> {
        Contact updatedContact = updatedVm.toContact();
        contactManager.updateContact(updatedContact);
        selected.updateFrom(updatedContact);
        // 为了刷新ListView对应的Cell
        int index = masterData.indexOf(selected);
        masterData.set(index, selected);
      });
    } else {
      Alert alert = new Alert(Alert.AlertType.WARNING, "请选择目标对象");
      alert.setTitle("提示");
      alert.setHeaderText(null);
      alert.showAndWait();
    }
  }

  @FXML
  private void onDeleteContact() {
    ObservableList<ContactViewModel> selectedItems = contactListView.getSelectionModel().getSelectedItems();
    if (!selectedItems.isEmpty()) {
      StringBuilder names = new StringBuilder();
      for (int i = 0; i < selectedItems.size(); i++) {
        names.append(selectedItems.get(i).getName());
        if (i < selectedItems.size() - 1) {
          names.append(", ");
        }
        if (i == 4 && selectedItems.size() > 5) {
          names.append("等 ").append(selectedItems.size()).append(" 人");
          break;
        }
      }

      Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要删除联系人 " + names.toString() + " 吗？");
      alert.setTitle("确认删除");
      alert.setHeaderText(null);
      Optional<ButtonType> alertResult = alert.showAndWait();
      if (alertResult.isPresent() && alertResult.get() == ButtonType.OK) {
        // Create a copy of the list to avoid ConcurrentModificationException during
        // removal
        List<ContactViewModel> toDelete = new java.util.ArrayList<>(selectedItems);
        for (ContactViewModel vm : toDelete) {
          contactManager.deleteContact(vm.toContact());
          masterData.remove(vm);
        }
        contactListView.getSelectionModel().clearSelection();
      }
    } else {
      Alert alert = new Alert(Alert.AlertType.WARNING, "请选择目标对象");
      alert.setTitle("提示");
      alert.setHeaderText(null);
      alert.showAndWait();
    }
  }

  @FXML
  private void onImport() {
    java.util.List<String> choices = new java.util.ArrayList<>();
    choices.add("1. 导入 CSV 文件");
    choices.add("2. 导入 vCard 文件");

    ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
    dialog.setTitle("导入");
    dialog.setHeaderText("请选择要导入的文件格式");
    dialog.setContentText("操作类型:");

    Optional<String> result = dialog.showAndWait();
    if (result.isPresent()) {
      String choice = result.get();

      javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
      if (choice.contains("CSV")) {
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
      } else {
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("vCard Files", "*.vcf"));
      }

      javafx.stage.Window window = contactListView.getScene().getWindow();

      try {
        java.io.File file = fileChooser.showOpenDialog(window);
        if (file != null) {
          java.util.List<Contact> imported = choice.startsWith("1")
              ? com.addressbook.utils.ContactImportExportUtil.importFromCsv(file)
              : com.addressbook.utils.ContactImportExportUtil.importFromVCard(file);

          int count = 0;
          for (Contact c : imported) {
            contactManager.addContact(c);
            masterData.add(new ContactViewModel(c));
            count++;
          }
          Alert alert = new Alert(Alert.AlertType.INFORMATION, "成功导入 " + count + " 条联系人数据。");
          alert.showAndWait();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        new Alert(Alert.AlertType.ERROR, "导入失败: " + ex.getMessage()).showAndWait();
      }
    }
  }

  @FXML
  private void onExport() {
    java.util.List<String> choices = new java.util.ArrayList<>();
    choices.add("1. 导出全部为 CSV");
    choices.add("2. 导出选中为 CSV");
    choices.add("3. 导出全部为 vCard");
    choices.add("4. 导出选中为 vCard");

    ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
    dialog.setTitle("导出");
    dialog.setHeaderText("请选择导出格式和范围");
    dialog.setContentText("操作类型:");

    Optional<String> result = dialog.showAndWait();
    if (result.isPresent()) {
      String choice = result.get();

      javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
      if (choice.contains("CSV")) {
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
      } else {
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("vCard Files", "*.vcf"));
      }

      javafx.stage.Window window = contactListView.getScene().getWindow();

      try {
        java.io.File file = fileChooser.showSaveDialog(window);
        if (file != null) {
          java.util.List<Contact> toExport = new java.util.ArrayList<>();
          if (choice.startsWith("1") || choice.startsWith("3")) {
            for (ContactViewModel vm : masterData) {
              toExport.add(vm.toContact());
            }
          } else {
            // 选中
            for (ContactViewModel vm : contactListView.getSelectionModel().getSelectedItems()) {
              toExport.add(vm.toContact());
            }
          }

          if (toExport.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "没有可导出的数据！").showAndWait();
            return;
          }

          if (choice.contains("CSV")) {
            com.addressbook.utils.ContactImportExportUtil.exportToCsv(toExport, file);
          } else {
            com.addressbook.utils.ContactImportExportUtil.exportToVCard(toExport, file);
          }
          new Alert(Alert.AlertType.INFORMATION, "成功导出 " + toExport.size() + " 条联系人数据。").showAndWait();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        new Alert(Alert.AlertType.ERROR, "导出失败: " + ex.getMessage()).showAndWait();
      }
    }
  }

  // --- 内部类：自定义卡片渲染的 ListCell ---
  private class ContactListCell extends ListCell<ContactViewModel> {
    private BorderPane rootContainer;
    private ImageView avatarImageView;
    private Label nameLabel;
    private Label groupLabel;
    private FlowPane detailsPane;

    public ContactListCell() {
      super();
      getStyleClass().add("contact-list-cell");

      avatarImageView = new ImageView();
      avatarImageView.setFitWidth(60);
      avatarImageView.setFitHeight(60);
      javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(30, 30, 30);
      avatarImageView.setClip(clip);

      nameLabel = new Label();
      nameLabel.getStyleClass().add("contact-name");

      groupLabel = new Label();
      groupLabel.getStyleClass().add("contact-group-tag");

      HBox topBox = new HBox(10, nameLabel, groupLabel);
      topBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

      detailsPane = new FlowPane(10, 8);

      VBox rightBox = new VBox(8, topBox, detailsPane);
      HBox.setHgrow(rightBox, Priority.ALWAYS);

      rootContainer = new BorderPane();
      rootContainer.setLeft(avatarImageView);
      BorderPane.setMargin(avatarImageView, new Insets(0, 15, 0, 0));
      rootContainer.setCenter(rightBox);

      rootContainer.getStyleClass().add("contact-card");

      // --- 添加拖拽源逻辑 ---
      setOnDragDetected(event -> {
        if (getItem() != null) {
          // 启动拖拽
          javafx.scene.input.Dragboard db = startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
          javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
          // 放入一个标记字符串即可，实际操作根据 ListView 的选中项决定
          content.putString("CONTACT_DRAG");
          db.setContent(content);

          // 可选：设置拖拽时的截图为卡片的 Snapshot
          javafx.scene.image.WritableImage snapshot = rootContainer.snapshot(new javafx.scene.SnapshotParameters(),
              null);
          db.setDragView(snapshot);

          event.consume();
        }
      });

      setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !isEmpty()) {
          onEditContact();
        }
      });
    }

    private Label createDetailLabel(String prefix, String value) {
      Label label = new Label(prefix + value);
      label.getStyleClass().add("contact-detail-tag");
      return label;
    }

    @Override
    protected void updateItem(ContactViewModel item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setGraphic(null);
      } else {
        nameLabel.setText(item.getName());
        groupLabel.setText("分组: " + (item.getGroup() == null || item.getGroup().isEmpty() ? "未分组" : item.getGroup()));

        detailsPane.getChildren().clear();
        if (item.getPhone() != null && !item.getPhone().isEmpty())
          detailsPane.getChildren().add(createDetailLabel("电话: ", item.getPhone()));
        if (item.getMobile() != null && !item.getMobile().isEmpty())
          detailsPane.getChildren().add(createDetailLabel("手机: ", item.getMobile()));
        if (item.getEmail() != null && !item.getEmail().isEmpty())
          detailsPane.getChildren().add(createDetailLabel("邮件: ", item.getEmail()));
        if (item.getImAccount() != null && !item.getImAccount().isEmpty())
          detailsPane.getChildren().add(createDetailLabel("通讯工具: ", item.getImAccount()));
        if (item.getHomepage() != null && !item.getHomepage().isEmpty())
          detailsPane.getChildren().add(createDetailLabel("主页: ", item.getHomepage()));
        if (item.getBirthday() != null && !item.getBirthday().isEmpty())
          detailsPane.getChildren().add(createDetailLabel("生日: ", item.getBirthday()));
        if (item.getCompany() != null && !item.getCompany().isEmpty())
          detailsPane.getChildren().add(createDetailLabel("单位: ", item.getCompany()));
        if (item.getAddress() != null && !item.getAddress().isEmpty())
          detailsPane.getChildren().add(createDetailLabel("地址: ", item.getAddress()));
        if (item.getZipcode() != null && !item.getZipcode().isEmpty())
          detailsPane.getChildren().add(createDetailLabel("邮编: ", item.getZipcode()));
        if (item.getRemark() != null && !item.getRemark().isEmpty())
          detailsPane.getChildren().add(createDetailLabel("备注: ", item.getRemark()));

        if (item.getPhotoBase64() != null && !item.getPhotoBase64().isEmpty()) {
          try {
            byte[] imgData = Base64.getDecoder().decode(item.getPhotoBase64());
            Image img = new Image(new ByteArrayInputStream(imgData));
            avatarImageView.setImage(img);
          } catch (Exception e) {
            e.printStackTrace();
            avatarImageView.setImage(null);
          }
        } else {
          avatarImageView.setImage(null);
        }

        setGraphic(rootContainer);
      }
    }
  }

  private Dialog<ContactViewModel> createContactDialog(String title, ContactViewModel existing) {
    Dialog<ContactViewModel> dialog = new Dialog<>();
    dialog.setTitle(title);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 20, 10, 10));

    TextField nameField = new TextField();
    nameField.setPromptText("必填");
    TextField phoneField = new TextField();
    TextField emailField = new TextField();
    TextField birthdayField = new TextField();
    TextField companyField = new TextField();
    TextField addressField = new TextField();
    TextField zipcodeField = new TextField();
    TextField groupField = new TextField();
    TextField remarkField = new TextField();

    // 图片上传相关
    Button uploadImageBtn = new Button("上传头像");
    Label imageNameLabel = new Label("未选择图片");
    final String[] currentPhotoBase64 = new String[1];

    uploadImageBtn.setOnAction(e -> {
      javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
      fileChooser.getExtensionFilters().addAll(
          new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
      java.io.File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
      if (selectedFile != null) {
        try {
          byte[] fileContent = java.nio.file.Files.readAllBytes(selectedFile.toPath());
          currentPhotoBase64[0] = java.util.Base64.getEncoder().encodeToString(fileContent);
          imageNameLabel.setText(selectedFile.getName());
        } catch (java.io.IOException ex) {
          ex.printStackTrace();
        }
      }
    });

    if (existing != null) {
      nameField.setText(existing.getName());
      phoneField.setText(existing.getPhone());
      emailField.setText(existing.getEmail());
      birthdayField.setText(existing.getBirthday());
      companyField.setText(existing.getCompany());
      addressField.setText(existing.getAddress());
      zipcodeField.setText(existing.getZipcode());
      groupField.setText(existing.getGroup());
      remarkField.setText(existing.getRemark());
      if (existing.getPhotoBase64() != null && !existing.getPhotoBase64().isEmpty()) {
        currentPhotoBase64[0] = existing.getPhotoBase64();
        imageNameLabel.setText("已上传(点此重新上传)");
      }
    }

    grid.add(new Label("*姓名:"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("电话:"), 0, 1);
    grid.add(phoneField, 1, 1);
    grid.add(new Label("邮箱:"), 0, 2);
    grid.add(emailField, 1, 2);
    grid.add(new Label("头像:"), 0, 3);
    grid.add(uploadImageBtn, 1, 3);
    grid.add(imageNameLabel, 1, 4);

    grid.add(new Label("生日:"), 2, 0);
    grid.add(birthdayField, 3, 0);
    grid.add(new Label("单位:"), 2, 1);
    grid.add(companyField, 3, 1);
    grid.add(new Label("地址:"), 2, 2);
    grid.add(addressField, 3, 2);
    grid.add(new Label("邮编:"), 2, 3);
    grid.add(zipcodeField, 3, 3);
    grid.add(new Label("分组:"), 2, 4);
    grid.add(groupField, 3, 4);
    grid.add(new Label("备注:"), 2, 5);
    grid.add(remarkField, 3, 5);

    // 确定按钮的校验拦截
    javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
    okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
      if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "姓名不能为空！");
        alert.showAndWait();
        event.consume(); // 阻断弹窗关闭
      }
    });

    dialog.getDialogPane().setContent(grid);
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        Contact c = new Contact();
        if (existing != null) {
          c.setId(existing.getId());
          c.setMobile(existing.getMobile());
          c.setImAccount(existing.getImAccount());
          c.setHomepage(existing.getHomepage());
        }
        c.setName(nameField.getText());
        c.setPhone(phoneField.getText());
        c.setEmail(emailField.getText());
        c.setBirthday(birthdayField.getText());
        c.setCompany(companyField.getText());
        c.setAddress(addressField.getText());
        c.setZipcode(zipcodeField.getText());
        c.setGroup(groupField.getText());
        c.setRemark(remarkField.getText());
        c.setPhotoBase64(currentPhotoBase64[0]);
        return new ContactViewModel(c);
      }
      return null;
    });
    return dialog;
  }
}
