package com.freezlex.jamesbot.database

import com.freezlex.jamesbot.database.entities.Guild
import com.freezlex.jamesbot.database.entities.GuildSettings
import com.freezlex.jamesbot.database.entities.User
import com.freezlex.jamesbot.database.entities.UserSettings
import com.freezlex.jamesbot.internals.client.ClientSettings
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

/** Database class */
class Database (private var clientSettings: ClientSettings){

    private val tables: List<Table> = listOf(User, Guild, UserSettings, GuildSettings)

    init{
        connect()
        initTables()
    }

    /**
     * Connect the database
     */
    private fun connect(){
        Database.connect(
            "jdbc:mysql://${clientSettings.dbHost}:${clientSettings.dbPort}/${clientSettings.dbName}",
            "com.mysql.cj.jdbc.Driver",
            clientSettings.dbUser,
            clientSettings.dbPassword)
    }

    /**
     * Initialize all the table provided
     */
    private fun initTables(){
        for(table in tables){
            transaction {
                SchemaUtils.create(table)
            }
        }
    }

}
