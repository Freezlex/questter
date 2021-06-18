package com.freezlex.jamesbot.implementation.moderation

import com.freezlex.jamesbot.internals.api.Context
import com.freezlex.jamesbot.internals.arguments.Argument
import com.freezlex.jamesbot.internals.commands.Cmd
import com.freezlex.jamesbot.internals.commands.CommandCategory
import net.dv8tion.jda.api.interactions.commands.OptionType

class TestModerationCommand: Cmd {
    override fun name(): String = "Test"
    override fun category() = CommandCategory.MODERATION

    fun run(ctx: Context, @Argument(type = OptionType.STRING) prefix: String){
        print("test")
    }
}
