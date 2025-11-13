module estruct_datos.laberintosoptimos {
    requires javafx.controls;
    requires javafx.fxml;


    opens estruct_datos.laberintosoptimos to javafx.fxml;
    exports estruct_datos.laberintosoptimos;
    exports estruct_datos.laberintosoptimos.Controladores;
    opens estruct_datos.laberintosoptimos.Controladores to javafx.fxml;
}