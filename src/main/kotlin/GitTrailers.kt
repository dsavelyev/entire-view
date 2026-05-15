package org.example

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler

fun getCheckpointId(proj: Project, root: VirtualFile, hash: String): String? {
    val handler = GitLineHandler(proj, root, GitCommand.SHOW).apply {
        setSilent(true)
        setStdoutSuppressed(false)
        addParameters(
            "-s",
            "--format=%(trailers:key=Entire-Checkpoint-Id,valueonly,unfold)",
            hash,
        )
    }

    val result = Git.getInstance().runCommand(handler)
    if (!result.success()) return null

    return result.output
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() }
}