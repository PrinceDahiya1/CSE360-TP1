package guiAdminUserList;

import java.util.ArrayList;
import java.util.Optional;
import entityClasses.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*******
 * <p> Title: ViewAdminUserList Class </p>
 * 
 * <p> Description: The Java/FX-based View for the Admin User List. This class provides the 
 * user interface for displaying a comprehensive list of all users registered in the system. 
 * 
 * It presents the data (Username, Name, Email, and Roles) in a TableView and is designed 
 * to be displayed as a modal window, blocking interaction with other windows until closed. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Prince Dahiya
 * @version 1.00		2026-02-07 Implemented list users feature
 * @version 1.01		2026-02-08 Implemented delete functionality
 */

public class ViewAdminUserList {

	/**********
	 * <p> Method: display(Stage parentStage) </p>
	 * 
	 * <p> Description: This method is the entry point for the User List window. 
	 * It sets up the stage as a modal dialog, configures the TableView columns, 
	 * fetches the latest user data from the database, and displays the scene. </p>
	 * 
	 * @param parentStage The primary stage of the application, used to center this window (if needed)
	 */
    public static void display(Stage parentStage, boolean allowDelete) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL); // Block interaction with other windows
        window.setTitle(allowDelete ? "Delete a User" : "All Registered Users"); // Dynamic Title
		window.setMinWidth(800);

		Label label = new Label(allowDelete ? "Select a User to Delete" : "Registered Users");
		label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
		
        // --- Table Setup ---
        TableView<User> table = new TableView<>();
        
        // 1. Username Column
        TableColumn<User, String> userCol = new TableColumn<>("Username");
        userCol.setMinWidth(150);
        userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        // 2. Full Name Column (Combine First + Last)
        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setMinWidth(200);
        nameCol.setCellValueFactory(cellData -> {
            User u = cellData.getValue();
            return new SimpleStringProperty(u.getFirstName() + " " + u.getLastName());
        });

        // 3. Email Column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setMinWidth(250);
        emailCol.setCellValueFactory(new PropertyValueFactory<>("emailAddress"));

        // 4. Roles Column (Combine Booleans into String)
        TableColumn<User, String> roleCol = new TableColumn<>("Roles");
        roleCol.setMinWidth(150);
        roleCol.setCellValueFactory(cellData -> {
            User u = cellData.getValue();
            StringBuilder roles = new StringBuilder();
            if (u.getAdminRole()) roles.append("Admin, ");
            if (u.getNewRole1()) roles.append("Student, ");
            if (u.getNewRole2()) roles.append("Staff, ");
            
            // Remove trailing comma
            if (roles.length() > 0) roles.setLength(roles.length() - 2);
            return new SimpleStringProperty(roles.toString());
        });

        table.getColumns().addAll(userCol, nameCol, emailCol, roleCol);
        
        // Helper to refresh data
     	refreshTableData(table);
     	
     	// --- Buttons ---
     	HBox buttonBox = new HBox(20);
		buttonBox.setAlignment(Pos.CENTER);

		// CONDITIONAL LOGIC: Only add the Delete button if the mode is "true"
		if (allowDelete) {
			Button deleteButton = new Button("Delete Selected");
			deleteButton.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: darkred;");
			deleteButton.setOnAction(e -> {
				User selected = table.getSelectionModel().getSelectedItem();
				if (selected == null) {
					showAlert("No Selection", "Please select a user to delete.");
					return;
				}
				// Self-deletion check
				String currentUsername = guiAdminHome.ViewAdminHome.theUser.getUserName();
				if (selected.getUserName().equals(currentUsername)) {
					showAlert("Action Denied", "You cannot delete your own account.");
					return;
				}
				// Confirmation
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirm Deletion");
				alert.setHeaderText("Delete user: " + selected.getUserName() + "?");
				alert.setContentText("This action cannot be undone.");

				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					applicationMain.FoundationsMain.database.deleteUser(selected.getUserName());
					refreshTableData(table);
				}
			});
			buttonBox.getChildren().add(deleteButton);
		}

		Button closeButton = new Button("Close");
		closeButton.setOnAction(e -> window.close());
		buttonBox.getChildren().add(closeButton);

		// --- Layout ---
		VBox layout = new VBox(20);
		layout.getChildren().addAll(label, table, buttonBox);
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(20));

		Scene scene = new Scene(layout);
		window.setScene(scene);
		window.showAndWait();
    }

 	private static void refreshTableData(TableView<User> table) {
 		ArrayList<User> userList = applicationMain.FoundationsMain.database.getAllUsers();
 		ObservableList<User> observableList = FXCollections.observableArrayList(userList);
 		table.setItems(observableList);
 	}

 	private static void showAlert(String title, String content) {
 		Alert alert = new Alert(AlertType.WARNING);
 		alert.setTitle(title);
 		alert.setHeaderText(null);
 		alert.setContentText(content);
 		alert.showAndWait();
 	}
}