package guiAdminUserList;

import java.util.ArrayList;
import entityClasses.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
 * @version 1.00		2025-02-07 Implemented list users feature
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
    public static void display(Stage parentStage) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL); // Block interaction with other windows
        window.setTitle("All Registered Users");
        window.setMinWidth(800);

        Label label = new Label("Registered Users");
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

        // --- Fetch Data ---
        // Access the database via the main application reference
        ArrayList<User> userList = applicationMain.FoundationsMain.database.getAllUsers();
        ObservableList<User> observableList = FXCollections.observableArrayList(userList);
        table.setItems(observableList);

        // --- Close Button ---
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> window.close());

        // --- Layout ---
        VBox layout = new VBox(20);
        layout.getChildren().addAll(label, table, closeButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }
}