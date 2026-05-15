package org.example

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler

private fun runGit(handler: GitLineHandler, proj: Project): GitCommandResult? {
    val obj = object : Runnable {
        var result: GitCommandResult? = null

        override fun run(): Unit {
            result = Git.getInstance().runCommand(handler)
        }
    }
    ProgressManager.getInstance().runProcessWithProgressSynchronously(
        obj, "Running git show for Entire checkpoint", true, proj)
    return obj.result
}

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
    val listResult = runGit(listHandler, proj) ?: return null
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
    val showResult = runGit(showHandler, proj) ?: return null
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

    val result = runGit(handler, proj) ?: return null
    if (!result.success()) return null

    return result.output
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() }
}