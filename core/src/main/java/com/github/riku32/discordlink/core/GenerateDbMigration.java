package com.github.riku32.discordlink.core;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;

import java.io.IOException;
import java.nio.file.Path;

/**
 * THIS CLASS SHOULD ONLY BE CALLED THROUGH GRADLE EXECUTION TASKS<br>
 * TODO: Convert this to a gradle task with core as a dependency
 */
public class GenerateDbMigration {
    /**
     * Generate the DDL for the next DB migration.
     */
    public static void main(String[] args) throws IOException {
        System.out.println("DiscordLink migration generator");
        DbMigration dbMigration = DbMigration.create();
        dbMigration.setPathToResources(Path.of(System.getProperty("user.dir") + "/src/main/resources").toAbsolutePath().toString());
        dbMigration.addPlatform(Platform.H2);
        dbMigration.addPlatform(Platform.MYSQL);
        dbMigration.addPlatform(Platform.POSTGRES);
        dbMigration.generateMigration();
    }
}