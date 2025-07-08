

package com.panchoarc.restoreit.command;

import com.panchoarc.restoreit.db.DatabaseConnectorFactory;
import com.panchoarc.restoreit.db.DatabaseConnectorStrategy;
import com.panchoarc.restoreit.enums.DatabaseType;
import com.panchoarc.restoreit.utils.CommandAvailabilityChecker;
import com.panchoarc.restoreit.utils.InputPrompter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@Command(group = "Backup Commands")
public class BackupCommand {

    private final LineReader lineReader;
    private final InputPrompter inputPrompter;

    public BackupCommand(@Lazy LineReader lineReader, InputPrompter inputPrompter) {
        this.lineReader = lineReader;
        this.inputPrompter = inputPrompter;
    }


    @Command(command = "backup", alias = {"b"})
    public String crearBackup() {
        try {

            String tipoStr = inputPrompter.promptConAutocompletado("Tipo de base de datos", DatabaseType.allNames());
            DatabaseType dbType = DatabaseType.fromName(tipoStr)
                    .orElseThrow(() -> new IllegalArgumentException("Tipo no soportado"));

            String host = inputPrompter.promptConDefault(lineReader, "Host", dbType.getDefaultHost());
            String puerto = inputPrompter.promptConDefault(lineReader, "Puerto", dbType.getDefaultPort());
            String db = inputPrompter.promptObligatorio(lineReader, "Nombre de la base de datos");
            String user;
            String pass;
            if (dbType.requiresAuth()) {
                user = inputPrompter.promptObligatorio(lineReader, "Usuario");
                pass = inputPrompter.promptPassword(lineReader, "Contraseña");
            } else {
                user = inputPrompter.promptConDefault(lineReader, "Usuario (opcional)", "");
                pass = inputPrompter.promptConDefault(lineReader, "Contraseña (opcional)", "");
            }

            String path = inputPrompter.promptObligatorio(lineReader, "Ruta destino del backup (ej: backup.sql)");

            if (!CommandAvailabilityChecker.isCommandAvailable(dbType.getBackupCommand())) {
                return "\n❌ El comando de backup '" + dbType.getBackupCommand() + "' no está disponible.\n" + mensajeDeInstalacion(dbType);
            }

            DatabaseConnectorStrategy connector = DatabaseConnectorFactory.getConnector(dbType);
            if (!connector.canConnect(host, puerto, db, user, pass)) {
                return "\n❌ No se pudo conectar a la base de datos. Verifique los datos ingresados.";
            }

            ProcessBuilder builder = dbType.crearBackupProcess(host, puerto, db, user, pass, path);

            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return "\n✅ Backup realizado correctamente.";

            } else {
                return "\n❌ El backup falló con código: " + exitCode;
            }
        } catch (UserInterruptException | EndOfFileException e) {
            return "\n❌ Operación cancelada por el usuario.";
        } catch (IllegalArgumentException e) {
            return "\n⚠️ Entrada inválida: " + e.getMessage();
        } catch (Exception e) {

            System.out.println("MOSTRAR ERROR: " + e.getMessage());
            return "\n❌ Ocurrió un error inesperado: ";
        }
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