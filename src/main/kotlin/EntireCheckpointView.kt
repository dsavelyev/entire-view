package org.example

import com.intellij.ide.scratch.ScratchRootType
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.log.VcsLogDataKeys

class EntireCheckpointView : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val sel = e.getData(VcsLogDataKeys.VCS_LOG_COMMIT_SELECTION) ?: return
        val commit = sel.commits.firstOrNull() ?: return

        val checkpointId = getCheckpointId(project, commit.root, commit.hash.asString()) ?: return
        val prompt = getCheckpointPrompt(project, commit.root, checkpointId) ?: return

        val language = Language.findLanguageByID("Markdown") ?: Language.ANY
        val file = WriteAction.compute<VirtualFile?, Throwable> {
            ScratchRootType.getInstance().createScratchFile(project, "prompt.md", language, prompt)
        } ?: return
        FileEditorManager.getInstance(project).openFile(file, true)
    }
}