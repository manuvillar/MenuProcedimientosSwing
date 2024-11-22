package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MenuProcedimientosSwing {
    public static void main(String[] args) {
        // Crear la ventana principal
        JFrame frame = new JFrame("Consultas de Departamentos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLayout(new BorderLayout());

        // Panel superior para seleccionar la consulta
        JPanel selectionPanel = new JPanel(new FlowLayout());
        JLabel labelSeleccion = new JLabel("Selecciona una consulta:");
        String[] opciones = {"Consultar Nombre del Departamento", "Consultar Datos del Departamento"};
        JComboBox<String> comboBox = new JComboBox<>(opciones);
        selectionPanel.add(labelSeleccion);
        selectionPanel.add(comboBox);

        // Panel central para introducir el número de departamento
        JPanel inputPanel = new JPanel(new FlowLayout());
        JLabel labelDept = new JLabel("Número de Departamento:");
        JTextField deptField = new JTextField(10);
        JButton btnConsultar = new JButton("Consultar");
        inputPanel.add(labelDept);
        inputPanel.add(deptField);
        inputPanel.add(btnConsultar);

        // Área de resultados
        JTextArea resultArea = new JTextArea(8, 40);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Añadir los paneles al frame
        frame.add(selectionPanel, BorderLayout.NORTH);
        frame.add(inputPanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);

        // Acción del botón "Consultar"
        btnConsultar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String opcionSeleccionada = (String) comboBox.getSelectedItem();
                String depInput = deptField.getText().trim();

                // Validar que se haya introducido un número válido
                if (!depInput.matches("\\d+")) {
                    JOptionPane.showMessageDialog(frame, "Por favor, introduce un número válido para el departamento.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int depNumber = Integer.parseInt(depInput);

                try {
                    // Conexión a la base de datos
                    Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/practica", "root", "practica");

                    if ("Consultar Nombre del Departamento".equals(opcionSeleccionada)) {
                        // Llamar a la función almacenada `nombre_dep`
                        String sql = "{ ? = call nombre_dep(?) }";
                        CallableStatement llamada = conexion.prepareCall(sql);

                        llamada.registerOutParameter(1, Types.VARCHAR);
                        llamada.setInt(2, depNumber);

                        llamada.executeUpdate();
                        String nombreDep = llamada.getString(1);

                        resultArea.setText("Nombre del Departamento: " + nombreDep);

                        llamada.close();
                    } else if ("Consultar Datos del Departamento".equals(opcionSeleccionada)) {
                        // Llamar al procedimiento almacenado `datos_dept`
                        String sql = "{ call datos_dept(?, ?, ?) }";
                        CallableStatement llamada = conexion.prepareCall(sql);

                        llamada.setInt(1, depNumber);
                        llamada.registerOutParameter(2, Types.VARCHAR);
                        llamada.registerOutParameter(3, Types.VARCHAR);

                        llamada.executeUpdate();
                        String nombreDep = llamada.getString(2);
                        String localidadDep = llamada.getString(3);

                        resultArea.setText("Nombre del Departamento: " + nombreDep + "\n");
                        resultArea.append("Localidad del Departamento: " + localidadDep);

                        llamada.close();
                    }

                    conexion.close();
                } catch (SQLException ex) {
                    resultArea.setText("Error de conexión o consulta:\n" + ex.getMessage());
                }
            }
        });

        frame.setVisible(true);
    }
}