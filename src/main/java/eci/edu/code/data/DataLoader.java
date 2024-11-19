package eci.edu.code.data;

import eci.edu.code.model.Movement;
import eci.edu.code.model.User;
import eci.edu.code.service.MovementService;
import eci.edu.code.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private MovementService movementService;

    @Override
    public void run(String... args) throws Exception {
        // Verificar si el usuario Mateo ya existe
        String username = "MateoPalacios";
        String password = "Trueno";

        // Si el usuario no existe, lo creamos y agregamos movimientos
        if (userService.validateUser(username, password) == null) {
            User mateo = new User(username, password);
            userService.createUser(mateo);

            // Iniciar sesión para obtener el token
            String token = userService.validateUser(mateo.getUsername(), mateo.getPassword());

            if (token != null) {
                // Generar 5 ingresos
                for (int i = 0; i < 5; i++) {
                    Movement income = new Movement();
                    income.setUser(mateo);
                    income.setName("Producto Vendido " + (i + 1));
                    income.setValue(10000 + (i * 1000));
                    income.setType("income");
                    income.setDate(LocalDateTime.now().minusDays(i));
                    movementService.createMovement(income);
                }

                // Generar 5 salidas
                for (int i = 0; i < 5; i++) {
                    Movement outcome = new Movement();
                    outcome.setUser(mateo);
                    outcome.setName("Pago de Servicio " + (i + 1));
                    outcome.setValue(500 + (i * 100));
                    outcome.setType("outcome");
                    outcome.setDate(LocalDateTime.now().minusDays(i));
                    movementService.createMovement(outcome);
                }
            }
        }
    }
}
