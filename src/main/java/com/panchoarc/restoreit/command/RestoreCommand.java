package com.panchoarc.restoreit.command;

import com.panchoarc.restoreit.enums.DatabaseType;
import com.panchoarc.restoreit.utils.CommandAvailabilityChecker;
import com.panchoarc.restoreit.utils.InputPrompter;
import org.jline.reader.LineReader;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Command(group = "Restore Commands")
public class RestoreCommand {

    private final LineReader lineReader;
    private final InputPrompter inputPrompter;

    public RestoreCommand(@Lazy LineReader lineReader, InputPrompter inputPrompter) {
        this.lineReader = lineReader;
        this.inputPrompter = inputPrompter;
    }

    @Command(command = "restore", alias = {"r"})
    public void restore() {


        String tipoStr = inputPrompter.promptConAutocompletado("Tipo de base de datos", DatabaseType.allNames());
        DatabaseType dbType = DatabaseType.fromName(tipoStr)
                .orElseThrow(() -> new IllegalArgumentException("Tipo no soportado"));

        String host = inputPrompter.promptConDefault(lineReader, "Host", dbType.getDefaultHost());
        String port = inputPrompter.promptConDefault(lineReader, "Puerto", dbType.getDefaultPort());
        String dbName = inputPrompter.promptObligatorio(lineReader, "Nombre de la base de datos");
        String user;
        String password;
        if (dbType.requiresAuth()) {
            user = inputPrompter.promptObligatorio(lineReader, "Usuario");
            password = inputPrompter.promptPassword(lineReader, "Contraseña");
        } else {
            user = inputPrompter.promptConDefault(lineReader, "Usuario (opcional)", "");
            password = inputPrompter.promptConDefault(lineReader, "Contraseña (opcional)", "");
        }

        String filePath = inputPrompter.promptFilePath("Ruta destino del backup (ej: backup.sql)");


        File backupFile = new File(filePath);
        if (!backupFile.exists()) {
            System.err.println("El archivo especificado no existe: " + filePath);
            return;
        }

        if (!CommandAvailabilityChecker.isCommandAvailable(dbType.getBackupCommand())) {
            System.err.println("El comando de backup " + dbType.getBackupCommand() + "' no está disponible.\n" + mensajeDeInstalacion(dbType));
            return;
        }
        try {

            switch (dbType) {
                case MYSQL -> restoreMySQL(host, port, user, password, dbName, backupFile);
                case POSTGRESQL -> restorePostgres(host, port, user, password, dbName, backupFile);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error durante la restauración: " + e.getMessage());
        }
    }

    private void restorePostgres(String host, String port, String user, String password, String dbName, File filePath) throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder();
        builder.environment().put("PGPASSWORD", password); // Evita mostrar contraseña en CLI

        boolean isDump = filePath.getAbsolutePath().endsWith(".dump");

        if (isDump) {
            builder.command("pg_restore",
                    "-h", host,
                    "-p", port,
                    "-U", user,
                    "-d", dbName,
                    "--clean",
                    filePath.getAbsolutePath()
            );
        } else {
            builder.command("psql",
                    "-h", host,
                    "-p", port,
                    "-U", user,
                    "-d", dbName,
                    "-f", filePath.getAbsolutePath()
            );
        }

        // No usar inheritIO, para poder capturar la salida y errores
        Process process = builder.start();

        // Leer salida estándar (stdout)
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        // Leer salida de errores (stderr)
        try (var errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println(errorLine);
            }
        }

        int exitCode = process.waitFor();
        System.out.println(exitCode == 0 ? "✅ Restauración PostgreSQL exitosa." : "❌ Falló la restauración PostgreSQL.");
    }


    private void restoreMySQL(String host, String port, String user, String password, String dbName, File filePath) throws IOException, InterruptedException {


        ProcessBuilder builder = new ProcessBuilder(
                "mysql",
                "-h", host,
                "-P", port,
                "-u", user,
                "-p" + password,
                dbName
        );

        // Redirigir entrada correctamente
        builder.redirectInput(filePath.getAbsoluteFile());

        // Mejor usar salida capturada manualmente en vez de inheritIO para mayor control
        Process process = builder.start();

        // Leer y mostrar la salida del proceso (stdout)
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        // Leer y mostrar errores (stderr)
        try (var errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println(errorLine);
            }
        }

        int exitCode = process.waitFor();
        System.out.println(exitCode == 0 ? "✅ Restauración MySQL exitosa." : "❌ Falló la restauración MySQL.");
    }

    private String mensajeDeInstalacion(DatabaseType dbType) {
        return switch (dbType) {
            case MYSQL -> "Puedes descargar 'mysqldump' desde:\nhttps://dev.mysql.com/downloads/utilities/";
            case POSTGRESQL ->
                    "Puedes instalar 'pg_dump' con:\n- Linux: `sudo apt install postgresql-client`\n- macOS: `brew install libpq`\n- Windows: https://www.enterprisedb.com/downloads/postgres-postgresql-downloads";
            case MONGODB -> "Puedes descargar 'mongodump' desde:\nhttps://www.mongodb.com/try/download/database-tools";
            case SQLITE -> "Puedes descargar 'sqlite3' desde:\nhttps://www.sqlite.org/download.html";
        };
    }
}
