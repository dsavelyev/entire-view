package org.example

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler

fun getCheckpointPrompt(proj: Project, root: VirtualFile, checkpointId: String): String? {
    val shard1 = checkpointId.take(2)
    val shard2 = checkpointId.drop(2)
    val ref = "entire/checkpoints/v1"
    val dirPath = "$shard1/$shard2"

    val listHandler = GitLineHandler(proj, root, GitCommand.SHOW).apply {
        setSilent(true)
        setStdoutSuppressed(false)
        addParameters("$ref:$dirPath")
    }
    val listResult = Git.getInstance().runCommand(listHandler)
    if (!listResult.success()) return null

    val intSubdir = listResult.output
        .map { it.trimEnd('/') }
        .mapNotNull { it.toIntOrNull()?.let { n -> n to it } }
        .minByOrNull { it.first }
        ?.second ?: return null

    val showHandler = GitLineHandler(proj, root, GitCommand.SHOW).apply {
        setSilent(true)
        setStdoutSuppressed(false)
        addParameters("$ref:$dirPath/$intSubdir/prompt.txt")
    }
    val showResult = Git.getInstance().runCommand(showHandler)
    if (!showResult.success()) return null

    return showResult.output.joinToString("\n")
}

fun getCheckpointId(proj: Project, root: VirtualFile, hash: String): String? {
    val handler = GitLineHandler(proj, root, GitCommand.SHOW).apply {
        setSilent(true)
        setStdoutSuppressed(false)
        addParameters(
            "-s",
            "--format=%(trailers:key=Entire-Checkpoint,valueonly,unfold)",
            hash,
        )
    }

    val result = Git.getInstance().runCommand(handler)
    if (!result.success()) return null

    return result.output
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() }
}