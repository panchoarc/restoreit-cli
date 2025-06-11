package com.panchoarc.restoreit.enums;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum DatabaseType {
    MYSQL("mysql", "localhost", "3306", "mysqldump", "mysql",true),
    POSTGRESQL("postgresql", "localhost", "5432", "pg_dump", "psql",true),
    MONGODB("mongodb", "localhost", "27017", "mongodump", "mongorestore",false),
    SQLITE("sqlite", "", "", "sqlite3", "sqlite3",false);

    private final String name;
    private final String defaultHost;
    private final String defaultPort;
    private final String backupCommand;
    private final String restoreCommand;

    private final boolean requiresAuth;

    DatabaseType(String name, String defaultHost, String defaultPort, String backupCommand, String restoreCommand, boolean requiresAuth) {
        this.name = name;
        this.defaultHost = defaultHost;
        this.defaultPort = defaultPort;
        this.backupCommand = backupCommand;
        this.restoreCommand = restoreCommand;
        this.requiresAuth = requiresAuth;
    }

    public String getName() {
        return name;
    }

    public String getDefaultHost() {
        return defaultHost;
    }

    public String getDefaultPort() {
        return defaultPort;
    }

    public String getBackupCommand() {
        return backupCommand;
    }

    public String getRestoreCommand() {
        return restoreCommand;
    }

    public boolean requiresAuth() {
        return requiresAuth;
    }

    public static Optional<DatabaseType> fromName(String name) {
        return Arrays.stream(values())
                .filter(db -> db.name.equalsIgnoreCase(name))
                .findFirst();
    }

    public static List<String> allNames() {
        return Arrays.stream(values())
                .map(DatabaseType::getName)
                .toList();
    }

    public ProcessBuilder crearBackupProcess(String host, String puerto, String db, String user, String pass, String outputPath) throws IOException {
        List<String> comando;
        ProcessBuilder builder;

        // 👉 Normaliza el path del archivo de salida
        File outputFile = resolverRutaDeBackup(outputPath, db);
        String absoluteOutput = outputFile.getAbsolutePath();

        switch (this) {
            case MYSQL -> {
                File cnf = crearArchivoMyCnfTemporal(host, user, pass);
                comando = List.of(
                        "mysqldump",
                        "--defaults-extra-file=" + cnf.getAbsolutePath(),
                        "-P", puerto,
                        db
                );
                builder = new ProcessBuilder(comando);
                builder.redirectOutput(outputFile);
            }

            case POSTGRESQL -> {
                comando = List.of(
                        "pg_dump", "-h", host, "-p", puerto,
                        "-U", user, "-b", "-v", "-f", absoluteOutput, db
                );
                builder = new ProcessBuilder(comando);
                builder.environment().put("PGPASSWORD", pass);
            }

            case MONGODB -> {
                comando = new ArrayList<>(List.of(
                        "mongodump", "--host", host, "--port", puerto,
                        "--db", db, "--out", outputFile.getParent()
                ));
                if (user != null && !user.isBlank()) {
                    comando.add("-u");
                    comando.add(user);
                }
                if (pass != null && !pass.isBlank()) {
                    comando.add("-p");
                    comando.add(pass);
                }
                builder = new ProcessBuilder(comando);
            }

            case SQLITE -> {
                comando = List.of("cp", db, absoluteOutput);
                builder = new ProcessBuilder(comando);
            }

            default -> throw new UnsupportedOperationException("Tipo de base no soportado");
        }

        builder.directory(new File(System.getProperty("user.dir")));
        builder.redirectErrorStream(true);
        return builder;
    }


    public static File resolverRutaDeBackup(String outputPath, String dbName) throws IOException {
        File rawOutput = new File(outputPath);

        boolean endsWithSlash = outputPath.endsWith("/") || outputPath.endsWith("\\");
        boolean hasExtension = outputPath.matches(".*\\.(sql|gz|dump|bak|zip|tar|7z)$");

        File outputDir;
        File outputFile;

        if (rawOutput.exists() && rawOutput.isDirectory() || (!hasExtension && !rawOutput.exists()) || endsWithSlash) {
            // Es un directorio o parece uno
            outputDir = rawOutput.isAbsolute() ? rawOutput : new File(System.getProperty("user.dir"), outputPath);
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String filename = String.format("%s-backup-%s.sql", dbName, timestamp);
            outputFile = new File(outputDir, filename);
        } else {
            // Es un archivo
            outputFile = rawOutput.isAbsolute() ? rawOutput : new File(System.getProperty("user.dir"), outputPath);
            outputDir = outputFile.getParentFile();
        }

        // Intenta crear el directorio si no existe
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created) {
                throw new IOException("No se pudo crear la carpeta de destino: " + outputDir.getAbsolutePath());
            }
        }

        return outputFile;
    }


    private File crearArchivoMyCnfTemporal(String host, String user, String pass) throws IOException {
        File tempFile = File.createTempFile("mysql-", ".cnf");
        tempFile.deleteOnExit();

        String contenido = """
        [client]
        user=%s
        password=%s
        host=%s
        """.formatted(user, pass, host);

        java.nio.file.Files.writeString(tempFile.toPath(), contenido);

        // Solo lectura para el usuario actual (seguridad en Unix)
        tempFile.setReadable(false, false); // No readable por otros
        tempFile.setReadable(true, true);   // Sí por el dueño

        return tempFile;
    }
}
