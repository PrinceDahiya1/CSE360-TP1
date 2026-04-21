/*
 * Module definition for the Foundations SP26 project.
 */
module FoundationsF25 {
	requires javafx.controls;
	requires java.sql;
	requires org.junit.jupiter.api;
	
	opens applicationMain to javafx.graphics, javafx.fxml;
	opens entityClasses to javafx.base;
}
