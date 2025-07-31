module com.mycompany.rental_playstation {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    
    opens com.mycompany.manufacturing_system to javafx.fxml;
    exports com.mycompany.manufacturing_system;
}
