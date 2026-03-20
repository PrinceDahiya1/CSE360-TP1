/*
 * Module definition for the Foundations SP26 project.
 */
module FoundationsF25 {
	requires javafx.controls;
	requires java.sql;
	
	opens applicationMain to javafx.graphics, javafx.fxml;
	opens entityClasses to javafx.base;
}
