package com.freezlex.jamesbot.internals.api.exceptions

import com.freezlex.jamesbot.internals.api.Context
import com.freezlex.jamesbot.internals.commands.Cmd
import com.freezlex.jamesbot.internals.commands.CommandFunction
import com.freezlex.jamesbot.logger
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class CommandEvent: CommandEventAdapter {

    override fun onCommandError(ctx: Context, command: CommandFunction, error: Throwable) {
        error.printStackTrace()
    }

    override fun onCommandPostInvoke(ctx: Context, command: CommandFunction, failed: Boolean) {
        logger.error("Failed to execute command")
    }

    override fun onCommandPreInvoke(ctx: Context, command: CommandFunction) = true

    override fun onParseError(ctx: Context, command: CommandFunction, error: Throwable) {
        error.printStackTrace()
    }

    override fun onInternalError(error: Throwable) {
        error.printStackTrace()
    }

    override fun onCommandCooldown(ctx: Context, command: CommandFunction, cooldown: Long) = ctx.message.reply("The command `${command.name}` is under cooldown. Please wait `$cooldown seconds` before trying again.").queue()

    override fun onBotMissingPermissions(ctx: Context, command: CommandFunction, permissions: List<Permission>) = ctx.message.reply("Uh ... I'm missing some permission for this action. Missing permissions : `${permissions.map { it.getName() }.joinToString { "${it}, `" }}`").queue()

    override fun onUserMissingPermissions(ctx: Context, command: CommandFunction, permissions: List<Permission>) = ctx.message.reply("Uh ... You are missing some permission for this action. Missing permissions : `${permissions.map { it.getName() }.joinToString { "${it}, `" }}`").queue()

    override fun onUnknownCommand(event: MessageReceivedEvent, command: String, args: List<String>) = event.message.reply("Hé gros t'as fumé ? Je connais pas la commande  `${command}`").queue()

    override fun onBadArgument(ctx: Context, cmd: CommandFunction, e: Throwable) = ctx.message.reply("Bad argument in `${cmd}`. $e").queue()
}
