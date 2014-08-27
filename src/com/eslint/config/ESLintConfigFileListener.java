package com.eslint.config;

import com.eslint.ESLintProjectComponent;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class ESLintConfigFileListener {
    private final Project project;
    private final AtomicBoolean LISTENING = new AtomicBoolean(false);

    public ESLintConfigFileListener(@NotNull Project project) {
        this.project = project;
    }

    private void startListener() {
        if (LISTENING.compareAndSet(false, true))
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            VirtualFileManager.getInstance().addVirtualFileListener(new ESLintConfigFileVfsListener(), ESLintConfigFileListener.this.project);
                            EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
                            multicaster.addDocumentListener(new ESLintConfigFileDocumentListener(), ESLintConfigFileListener.this.project);
                        }
                    });
                }
            });
    }

    public static void start(@NotNull Project project) {
        ESLintConfigFileListener listener = ServiceManager.getService(project, ESLintConfigFileListener.class);
        listener.startListener();
    }

    private void fileChanged(@NotNull VirtualFile file) {
        if (ESLintConfigFileUtil.isESLintConfigFile(file) && !project.isDisposed()) {
            restartAnalyzer();
        }
    }

    private void restartAnalyzer() {
        ESLintProjectComponent component = project.getComponent(ESLintProjectComponent.class);
        if (component.isEnabled()) {
            DaemonCodeAnalyzer.getInstance(project).restart();
        }
    }

    /**
     * VFS Listener
     */
    private class ESLintConfigFileVfsListener extends VirtualFileAdapter {
        private ESLintConfigFileVfsListener() {
        }

        public void fileCreated(@NotNull VirtualFileEvent event) {
            ESLintConfigFileListener.this.fileChanged(event.getFile());
        }

        public void fileDeleted(@NotNull VirtualFileEvent event) {
            ESLintConfigFileListener.this.fileChanged(event.getFile());
        }

        public void fileMoved(@NotNull VirtualFileMoveEvent event) {
            ESLintConfigFileListener.this.fileChanged(event.getFile());
        }

        public void fileCopied(@NotNull VirtualFileCopyEvent event) {
            ESLintConfigFileListener.this.fileChanged(event.getFile());
            ESLintConfigFileListener.this.fileChanged(event.getOriginalFile());
        }
    }

    /**
     * Document Listener
     */
    private class ESLintConfigFileDocumentListener extends DocumentAdapter {
        private ESLintConfigFileDocumentListener() {
        }

        public void documentChanged(DocumentEvent event) {
            VirtualFile file = FileDocumentManager.getInstance().getFile(event.getDocument());
            if (file != null) {
                ESLintConfigFileListener.this.fileChanged(file);
            }
        }
    }
}

