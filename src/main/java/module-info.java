module pos.system {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    
    exports com.pos;
    exports com.pos.controllers;
    exports com.pos.models;
    exports com.pos.repository;
    exports com.pos.database;
    exports com.pos.utils;
}
