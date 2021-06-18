package com.freezlex.jamesbot.implementation.moderation

import com.freezlex.jamesbot.internals.api.Context
import com.freezlex.jamesbot.internals.arguments.Argument
import com.freezlex.jamesbot.internals.commands.Cmd
import com.freezlex.jamesbot.internals.commands.CommandCategory
import com.freezlex.jamesbot.internals.cooldown.BucketType
import com.freezlex.jamesbot.internals.cooldown.Cooldown
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.concurrent.TimeUnit

class PrefixCommand: Cmd {
    override fun name(): String = "Prefix"
    override fun category(): CommandCategory = CommandCategory.MODERATION
    override fun description(): String = "Change the prefix for the bot on this guild"
    override fun cooldown() = Cooldown(10, TimeUnit.SECONDS, BucketType.GUILD)
    override fun userPermissions() = listOf(Permission.ADMINISTRATOR)

    fun run(ctx: Context, @Argument(type = OptionType.STRING) prefix: String){

    }

}
