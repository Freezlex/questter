package com.freezlex.kohanato.core.indexer

import com.freezlex.kohanato.core.commands.contextual.Command
import com.freezlex.kohanato.core.commands.arguments.Argument
import com.freezlex.kohanato.core.commands.arguments.Param
import com.freezlex.kohanato.core.commands.contextual.BaseCommand
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.reflections.Reflections
import org.reflections.scanners.MethodParameterNamesScanner
import org.reflections.scanners.Scanners
import java.io.File
import java.lang.reflect.Modifier
import java.net.URL
import java.net.URLClassLoader
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

class Indexer {
    private val jar: Jar?
    private val pckgName: String
    private val reflections: Reflections
    private val classLoader: URLClassLoader?

    constructor(pckgName: String){
        this.pckgName = pckgName
        this.classLoader = null
        this.jar = null
        reflections = Reflections(pckgName, MethodParameterNamesScanner(), Scanners.SubTypes)
    }

    constructor(pckgName: String, jarPath: String){
        this.pckgName = pckgName
        val commandJar = File(jarPath)
        check(commandJar.exists()) { "jarPath points to a non-existent file. jarPath:${jarPath}" }
        check(commandJar.extension == "jar") { "jarPath leads to a file which is not a jar. jarPath:${jarPath}" }
        val path = URL("jar:file:${commandJar.absolutePath}!/")
        this.classLoader = URLClassLoader.newInstance(arrayOf(path))
        this.jar = Jar(commandJar.nameWithoutExtension, commandJar.absolutePath, pckgName, classLoader)
        reflections = Reflections(pckgName, this.classLoader, MethodParameterNamesScanner(), Scanners.SubTypes)
    }

    fun getCommands(): List<BaseCommand>{
        val command = reflections.getSubTypesOf(BaseCommand::class.java)
        return command.filter { !Modifier.isAbstract(it.modifiers) && !it.isInterface && BaseCommand::class.java.isAssignableFrom(it) }
            .map { it.getDeclaredConstructor().newInstance() }
    }

    fun getCommand(command: BaseCommand): KFunction<*>{
        val itClass = command::class
        val runner = itClass.members.filterIsInstance<KFunction<*>>().firstOrNull { it.name == "run"}
        return runner!!
    }

    fun loadCommand(method: KFunction<*>, command: BaseCommand): Command {
        require(method.javaMethod!!.declaringClass == command::class.java){ "${method.name} is not from ${command::class.simpleName}" }
        val pck = command::class.java.`package`.name

        val name = command.name?.lowercase()?: pck.split('.').last().replace('_', ' ').lowercase()
        val category = command.category?.lowercase()?: pck.split('.').dropLast(1).last().replace('_', ' ').lowercase()
        val cooldown = command.cooldown
        val event = method.valueParameters.firstOrNull() { it.type.classifier?.equals(BaseCommand::class) == true}
        require(event != null) { "${method.name} is missing the event parameters!" }
        val arguments = loadParameters(method.valueParameters.filterNot { it.type.classifier?.equals(BaseCommand::class) == true })

        return Command(name, category, this.jar, cooldown, method, command, arguments, event)
    }

    private fun loadParameters(parameters: List<KParameter>): List<Argument>{
        val arguments = mutableListOf<Argument>()

        parameters.forEach {
            val p = it.findAnnotation<Param>()
            val name = if(p?.name == "" || p?.name == null) it.name.toString() else p.name
            val type = it.type.jvmErasure.javaObjectType
            val greedy = p?.greedy?: false
            val sType = p?.type?: OptionType.STRING
            val optional = it.isOptional
            val option = p?.options?: arrayOf()
            val nullable= it.type.isMarkedNullable
            val description = p?.description?: "No description provided"
            val tentative = if(p?.tentative == true && !(nullable || optional)) {
                throw IllegalStateException("${p.name} is marked as tentative, but does not have a default value and is not marked nullable!")
            }else{
                p?.tentative?: false
            }
            arguments.add(Argument(name, type, greedy, optional, nullable, tentative, sType, description, option.toMutableList(), it))
        }
        return arguments
    }
}