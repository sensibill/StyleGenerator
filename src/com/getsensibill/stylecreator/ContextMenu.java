package com.getsensibill.stylecreator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by dipendra on 7/11/16.
 */
public class ContextMenu extends AnAction {
    private static String BASE_THEME_FILE = Config.getConf().getString(Config.BASE_THEME);
    private static String WIDGET_THEME_FILE = Config.getConf().getString(Config.WIDGET_THEME);

    private static final String DEFAULT_NAME = "{{Theme.Name}}";
    public static String lastClassNameTyped = DEFAULT_NAME;

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        SelectionModel selectionModel = FileEditorManager.getInstance(project).getSelectedTextEditor().getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        int[] blockSelectionStarts = selectionModel.getBlockSelectionStarts();

        if (StringUtils.isEmpty(selectedText)) {
            Messages.showMessageDialog("No selection was made", "Please select xml tags", null);
            return;
        }

        if (editor == null) {
            return;
        }

        final Document document = editor.getDocument();

        if (document == null) {
            return;
        }
        if (!new File(BASE_THEME_FILE).exists()) {
            Messages.showMessageDialog(BASE_THEME_FILE + " in " + Config.getConf().getFile().getAbsolutePath() + " is not valid", "Invalid Path", null);
            return;
        }
        if (!new File(WIDGET_THEME_FILE).exists()) {
            Messages.showMessageDialog("File " + WIDGET_THEME_FILE + " in " + Config.getConf().getFile().getAbsolutePath() + " is not valid", "Invalid Path", null);
            return;
        }


        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (virtualFile == null) {
            return;
        }

        StyleGenerator styleGenerator = new StyleGenerator();
        final List<String> codeBlocks = styleGenerator.getCodeBlocks(selectedText, lastClassNameTyped == null ? "Style.Name" : lastClassNameTyped);

        String lastClassName = lastClassNameTyped;
        JOptionPane jOptionPane = new JOptionPane();
        JLabel jLabel = new JLabel(codeBlocks.get(0));
        jOptionPane.add(jLabel);
        jOptionPane.add(new JLabel("\n"));
        jOptionPane.add(new JLabel(codeBlocks.get(1)));
        jOptionPane.add(new JLabel("\n"));
        jOptionPane.add(new JLabel(codeBlocks.get(2)));
        Component component = jOptionPane;

        lastClassNameTyped = Messages.showInputDialog(project, "Snippet 1 \n" + codeBlocks.get(0) + "\nSnippet 2\n" + codeBlocks.get(1) + "\nSnippet 3\n" + codeBlocks.get(2), "Does this look right?", Messages.getQuestionIcon(), lastClassNameTyped, null, null);

        if (StringUtils.isEmpty(lastClassNameTyped) || lastClassNameTyped.equals(DEFAULT_NAME) || lastClassNameTyped.contains(" ")) {
            Messages.showMessageDialog("Please provide a valid class name.", "Operation Discarded", null);
            return;
        } else {
            lastClassNameTyped = lastClassNameTyped.trim();
        }

        for (int i = 0; i < codeBlocks.size(); i++) {

            String currentText = codeBlocks.get(i);
            currentText = currentText.replace(lastClassName, lastClassNameTyped);
            codeBlocks.remove(i);
            codeBlocks.add(i, currentText);
        }

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            @Override
                            public void run() {
                                boolean failed = true;

                                boolean placed = placeStyleToWidgetStyles(BASE_THEME_FILE, codeBlocks.get(1));
                                if (placed) {
                                    if (!placeStyleToWidgetStyles(WIDGET_THEME_FILE, codeBlocks.get(2))) {
                                        failed = true;
                                    } else {
                                        failed = false;
                                    }
                                } else {
                                    failed = true;
                                }
                                if (failed) {
                                    Messages.showMessageDialog("Failed to copy styles", "Operation Failed", null);
                                } else {
                                    document.replaceString(blockSelectionStarts[0], blockSelectionStarts[0] + selectedText.length(), codeBlocks.get(0));
                                }
                            }

                            private boolean placeStyleToWidgetStyles(String fileName, String styleText) {
                                try {
                                    boolean wasEndFound = false;
                                    List<String> stringList = FileUtils.readLines(new File(fileName), "UTF-8");
                                    for (int i = stringList.size() - 1; i >= 0; i--) {
                                        String str = stringList.get(i);
                                        if (str.contains("</resources>")) {
                                            stringList.add(i, styleText);
                                            wasEndFound = true;
                                            break;
                                        }
                                    }

                                    if (wasEndFound) {
                                        FileUtils.writeLines(new File(fileName), stringList, false);
                                    } else {
                                        return false;
                                    }

                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    return false;
                                }
                                return true;

                            }

                        });
                    }


                }, null, null);
            }
        });
    }
}
